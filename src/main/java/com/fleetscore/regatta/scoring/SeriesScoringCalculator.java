package com.fleetscore.regatta.scoring;

import com.fleetscore.regatta.domain.SeriesScoringType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesScoringCalculator {

    public record RegattaInput(
            Long regattaId,
            double coefficient,
            int fleetSize,
            List<SailorInput> sailors
    ) {}

    public record SailorInput(
            String sailorKey,
            String sailorName,
            String nationCode,
            String clubName,
            int netPoints,
            int rank
    ) {}

    public record RegattaScore(Long regattaId, double score, boolean excluded) {}

    public record SailorSeriesScore(
            String sailorKey,
            String sailorName,
            String nationCode,
            String clubName,
            int rank,
            double netScore,
            double totalScore,
            List<RegattaScore> regattaScores
    ) {}

    public List<SailorSeriesScore> calculate(
            SeriesScoringType type,
            List<RegattaInput> regattas,
            int throwoutAfter,
            int throwoutLimit
    ) {
        Map<String, SailorInfo> sailors = collectSailorInfo(regattas);
        Map<String, List<RegattaScore>> scoresByKey = computeRawScores(type, regattas, sailors);
        List<SailorSeriesScore> results = buildSailorScores(sailors, scoresByKey, throwoutAfter, throwoutLimit);
        return ranked(results);
    }

    private Map<String, SailorInfo> collectSailorInfo(List<RegattaInput> regattas) {
        Map<String, SailorInfo> sailors = new HashMap<>();
        for (RegattaInput regatta : regattas) {
            for (SailorInput sailor : regatta.sailors()) {
                sailors.putIfAbsent(sailor.sailorKey(), new SailorInfo(sailor.sailorName(), sailor.nationCode(), sailor.clubName()));
            }
        }
        return sailors;
    }

    private Map<String, List<RegattaScore>> computeRawScores(
            SeriesScoringType type,
            List<RegattaInput> regattas,
            Map<String, SailorInfo> sailors
    ) {
        Map<String, List<RegattaScore>> scoresByKey = new HashMap<>();
        sailors.keySet().forEach(key -> scoresByKey.put(key, new ArrayList<>()));

        for (RegattaInput regatta : regattas) {
            Map<String, SailorInput> sailorMap = buildSailorMap(regatta.sailors());
            for (String key : sailors.keySet()) {
                SailorInput sailor = sailorMap.get(key);
                if (sailor != null) {
                    double score = computeScore(type, sailor, regatta);
                    scoresByKey.get(key).add(new RegattaScore(regatta.regattaId(), score, false));
                }
            }
        }
        return scoresByKey;
    }

    private Map<String, SailorInput> buildSailorMap(List<SailorInput> sailors) {
        Map<String, SailorInput> map = new HashMap<>();
        for (SailorInput sailor : sailors) {
            map.put(sailor.sailorKey(), sailor);
        }
        return map;
    }

    private double computeScore(SeriesScoringType type, SailorInput sailor, RegattaInput regatta) {
        return switch (type) {
            case REGATTA_PLACES -> (double) sailor.rank();
            case SUM_RACE_POINTS -> (double) sailor.netPoints();
            case NORMALIZED -> (sailor.netPoints() / (double) (regatta.fleetSize() + 1)) * regatta.coefficient();
        };
    }

    private List<SailorSeriesScore> buildSailorScores(
            Map<String, SailorInfo> sailors,
            Map<String, List<RegattaScore>> scoresByKey,
            int throwoutAfter,
            int throwoutLimit
    ) {
        List<SailorSeriesScore> results = new ArrayList<>();
        for (Map.Entry<String, SailorInfo> entry : sailors.entrySet()) {
            String key = entry.getKey();
            SailorInfo info = entry.getValue();
            List<RegattaScore> scores = scoresByKey.get(key);
            results.add(buildScore(key, info, scores, throwoutAfter, throwoutLimit));
        }
        return results;
    }

    private SailorSeriesScore buildScore(
            String key,
            SailorInfo info,
            List<RegattaScore> scores,
            int throwoutAfter,
            int throwoutLimit
    ) {
        int throwouts = calculateThrowouts(scores.size(), throwoutAfter, throwoutLimit);
        List<RegattaScore> markedScores = applyThrowouts(scores, throwouts);
        double totalScore = scores.stream().mapToDouble(RegattaScore::score).sum();
        double netScore = markedScores.stream()
                .filter(s -> !s.excluded())
                .mapToDouble(RegattaScore::score)
                .sum();
        return new SailorSeriesScore(key, info.name(), info.nationCode(), info.clubName(), 0, netScore, totalScore, markedScores);
    }

    private List<RegattaScore> applyThrowouts(List<RegattaScore> scores, int throwouts) {
        if (throwouts <= 0) {
            return scores;
        }
        List<IndexedScore> indexed = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            indexed.add(new IndexedScore(i, scores.get(i).score()));
        }
        indexed.sort(Comparator.comparingDouble(IndexedScore::score).reversed());
        java.util.Set<Integer> excludedIndices = new java.util.HashSet<>();
        int toExclude = Math.min(throwouts, indexed.size());
        for (int i = 0; i < toExclude; i++) {
            excludedIndices.add(indexed.get(i).index());
        }
        List<RegattaScore> result = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            RegattaScore s = scores.get(i);
            result.add(new RegattaScore(s.regattaId(), s.score(), excludedIndices.contains(i)));
        }
        return result;
    }

    private int calculateThrowouts(int count, int throwoutAfter, int throwoutLimit) {
        if (throwoutAfter <= 0) {
            return 0;
        }
        int calculated = count / throwoutAfter;
        return throwoutLimit > 0 ? Math.min(calculated, throwoutLimit) : calculated;
    }

    private List<SailorSeriesScore> ranked(List<SailorSeriesScore> scores) {
        List<SailorSeriesScore> sorted = new ArrayList<>(scores);
        sorted.sort(Comparator
                .comparingDouble(SailorSeriesScore::netScore)
                .thenComparingDouble(SailorSeriesScore::totalScore)
                .thenComparing(SailorSeriesScore::sailorName));

        List<SailorSeriesScore> ranked = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0 && isDifferentRank(sorted.get(i - 1), sorted.get(i))) {
                rank = i + 1;
            }
            SailorSeriesScore s = sorted.get(i);
            ranked.add(new SailorSeriesScore(s.sailorKey(), s.sailorName(), s.nationCode(), s.clubName(),
                    rank, s.netScore(), s.totalScore(), s.regattaScores()));
        }
        return ranked;
    }

    private boolean isDifferentRank(SailorSeriesScore a, SailorSeriesScore b) {
        return a.netScore() != b.netScore() || a.totalScore() != b.totalScore();
    }

    private record SailorInfo(String name, String nationCode, String clubName) {}

    private record IndexedScore(int index, double score) {}
}
