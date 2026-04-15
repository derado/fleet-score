package com.fleetscore.regatta.scoring;

import java.util.ArrayList;
import java.util.Comparator;
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
        int numberOfStarters = (int) results.stream()
                .map(RaceResultInput::registrationId)
                .distinct()
                .count();
        int throwouts = calculateThrowouts(raceCount, throwoutAfter, throwoutLimit);
        return calculateScoresWithThrowouts(results, throwouts, numberOfStarters);
    }

    private List<SailorScore> calculateScoresWithThrowouts(List<RaceResultInput> results, int throwouts, int numberOfStarters) {
        Map<Long, List<RaceResultInput>> resultsByRegistration = results.stream()
                .collect(Collectors.groupingBy(RaceResultInput::registrationId));

        List<SailorScore> scores = new ArrayList<>();

        for (Map.Entry<Long, List<RaceResultInput>> entry : resultsByRegistration.entrySet()) {
            Long registrationId = entry.getKey();
            List<RaceResultInput> sailorResults = entry.getValue();
            scores.add(calculateSailorScore(registrationId, sailorResults, throwouts));
        }

        Comparator<SailorScore> tieComparator = Comparator
                .comparingInt(SailorScore::netPoints)
                .thenComparing(placeCountComparator(numberOfStarters))   // A8.1: sorted non-excluded scores
                .thenComparing(lastRaceComparator());                     // A8.2: last race first, incl excluded

        scores.sort(tieComparator);

        List<SailorScore> rankedScores = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < scores.size(); i++) {
            if (i > 0 && tieComparator.compare(scores.get(i - 1), scores.get(i)) != 0) {
                rank = i + 1;
            }
            SailorScore s = scores.get(i);
            rankedScores.add(new SailorScore(s.registrationId(), rank, s.netPoints(), s.totalPoints(), s.raceScores()));
        }

        return rankedScores;
    }

    // A8.1: sort non-excluded scores best→worst and compare lexicographically.
    // Equivalent to: most 1st places, then most 2nds, etc.
    // Scores > numberOfStarters are penalty scores and do not count as a finishing place.
    private Comparator<SailorScore> placeCountComparator(int numberOfStarters) {
        Comparator<SailorScore> cmp = Comparator.comparingInt(
                s -> -countPlace(s, 1, numberOfStarters));
        for (int place = 2; place <= numberOfStarters; place++) {
            int p = place;
            cmp = cmp.thenComparingInt(s -> -countPlace(s, p, numberOfStarters));
        }
        return cmp;
    }

    private int countPlace(SailorScore score, int place, int numberOfStarters) {
        return (int) score.raceScores().stream()
                .filter(r -> !r.excluded())
                .filter(r -> r.points() != null && r.points() <= numberOfStarters && r.points() == place)
                .count();
    }

    // A8.2: if A8.1 still ties, compare by score in the last race (incl excluded), then second-to-last, etc.
    private Comparator<SailorScore> lastRaceComparator() {
        return (a, b) -> {
            List<Integer> raceNumbers = a.raceScores().stream()
                    .map(RaceScore::raceNumber)
                    .sorted(Comparator.reverseOrder())
                    .toList();
            for (int raceNumber : raceNumbers) {
                int cmp = Integer.compare(pointsInRace(a, raceNumber), pointsInRace(b, raceNumber));
                if (cmp != 0) return cmp;
            }
            return 0;
        };
    }

    private int pointsInRace(SailorScore score, int raceNumber) {
        return score.raceScores().stream()
                .filter(r -> r.raceNumber().equals(raceNumber))
                .mapToInt(RaceScore::points)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
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
