package com.fleetscore.regatta.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LowPointScoringCalculator {

    public record RaceResultInput(
            Long registrationId,
            Long raceId,
            Integer raceNumber,
            Integer points,
            boolean isNonExcludable
    ) {}

    public record SailorScore(
            Long registrationId,
            int rank,
            int netPoints,
            int totalPoints,
            List<RaceScore> raceScores
    ) {}

    public record RaceScore(
            Long raceId,
            Integer raceNumber,
            Integer points,
            boolean excluded
    ) {}

    public int calculateThrowouts(int raceCount, int throwoutAfter, int throwoutLimit) {
        if (throwoutAfter <= 0) {
            return 0;
        }
        int calculatedThrowouts = raceCount / throwoutAfter;
        if (throwoutLimit > 0) {
            return Math.min(calculatedThrowouts, throwoutLimit);
        }
        return calculatedThrowouts;
    }

    public List<SailorScore> calculateScores(List<RaceResultInput> results, int throwoutAfter, int throwoutLimit) {
        int raceCount = (int) results.stream()
                .map(RaceResultInput::raceId)
                .distinct()
                .count();
        int throwouts = calculateThrowouts(raceCount, throwoutAfter, throwoutLimit);
        return calculateScoresWithThrowouts(results, throwouts);
    }

    private List<SailorScore> calculateScoresWithThrowouts(List<RaceResultInput> results, int throwouts) {
        Map<Long, List<RaceResultInput>> resultsByRegistration = results.stream()
                .collect(Collectors.groupingBy(RaceResultInput::registrationId));

        List<SailorScore> scores = new ArrayList<>();

        for (Map.Entry<Long, List<RaceResultInput>> entry : resultsByRegistration.entrySet()) {
            Long registrationId = entry.getKey();
            List<RaceResultInput> sailorResults = entry.getValue();

            SailorScore score = calculateSailorScore(registrationId, sailorResults, throwouts);
            scores.add(score);
        }

        scores.sort(Comparator.comparingInt(SailorScore::netPoints)
                .thenComparingInt(SailorScore::totalPoints));

        List<SailorScore> rankedScores = new ArrayList<>();
        for (int i = 0; i < scores.size(); i++) {
            SailorScore s = scores.get(i);
            rankedScores.add(new SailorScore(
                    s.registrationId(),
                    i + 1,
                    s.netPoints(),
                    s.totalPoints(),
                    s.raceScores()
            ));
        }

        return rankedScores;
    }

    private SailorScore calculateSailorScore(Long registrationId, List<RaceResultInput> results, int throwouts) {
        List<RaceResultInput> sortedResults = results.stream()
                .sorted(Comparator.comparingInt(RaceResultInput::raceNumber))
                .toList();

        int totalPoints = sortedResults.stream()
                .mapToInt(RaceResultInput::points)
                .sum();

        List<Integer> excludableIndices = findExcludableRaces(sortedResults, throwouts);

        List<RaceScore> raceScores = new ArrayList<>();
        int netPoints = 0;

        for (int i = 0; i < sortedResults.size(); i++) {
            RaceResultInput result = sortedResults.get(i);
            boolean excluded = excludableIndices.contains(i);

            raceScores.add(new RaceScore(
                    result.raceId(),
                    result.raceNumber(),
                    result.points(),
                    excluded
            ));

            if (!excluded) {
                netPoints += result.points();
            }
        }

        return new SailorScore(registrationId, 0, netPoints, totalPoints, raceScores);
    }

    private List<Integer> findExcludableRaces(List<RaceResultInput> sortedResults, int throwouts) {
        if (throwouts <= 0) {
            return List.of();
        }

        List<Integer> excludableIndices = new ArrayList<>();

        List<IndexedResult> indexedResults = new ArrayList<>();
        for (int i = 0; i < sortedResults.size(); i++) {
            RaceResultInput result = sortedResults.get(i);
            if (!result.isNonExcludable()) {
                indexedResults.add(new IndexedResult(i, result.points()));
            }
        }

        indexedResults.sort((a, b) -> Integer.compare(b.points, a.points));

        int toExclude = Math.min(throwouts, indexedResults.size());
        for (int i = 0; i < toExclude; i++) {
            excludableIndices.add(indexedResults.get(i).index);
        }

        return excludableIndices;
    }

    private record IndexedResult(int index, int points) {}
}
