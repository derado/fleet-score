package com.fleetscore.regatta.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LowPointScoringCalculatorTest {

    private LowPointScoringCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LowPointScoringCalculator();
    }

    @Test
    void calculateThrowouts_noThrowoutAfter_returnsZero() {
        assertThat(calculator.calculateThrowouts(10, 0, 0)).isEqualTo(0);
        assertThat(calculator.calculateThrowouts(10, 0, 5)).isEqualTo(0);
    }

    @Test
    void calculateThrowouts_afterFourRaces_oneThrowout() {
        assertThat(calculator.calculateThrowouts(3, 4, 0)).isEqualTo(0);
        assertThat(calculator.calculateThrowouts(4, 4, 0)).isEqualTo(1);
        assertThat(calculator.calculateThrowouts(7, 4, 0)).isEqualTo(1);
        assertThat(calculator.calculateThrowouts(8, 4, 0)).isEqualTo(2);
        assertThat(calculator.calculateThrowouts(12, 4, 0)).isEqualTo(3);
    }

    @Test
    void calculateThrowouts_withLimit_capsAtLimit() {
        assertThat(calculator.calculateThrowouts(4, 4, 1)).isEqualTo(1);
        assertThat(calculator.calculateThrowouts(8, 4, 1)).isEqualTo(1);
        assertThat(calculator.calculateThrowouts(12, 4, 2)).isEqualTo(2);
        assertThat(calculator.calculateThrowouts(16, 4, 2)).isEqualTo(2);
    }

    @Test
    void calculateScores_noThrowouts_ranksCorrectly() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 3, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 101L, 1, 2, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 102L, 2, 1, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 103L, 3, 2, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 0, 0);

        assertThat(scores).hasSize(2);

        LowPointScoringCalculator.SailorScore sailor1 = scores.stream()
                .filter(s -> s.registrationId().equals(2L))
                .findFirst().orElseThrow();
        LowPointScoringCalculator.SailorScore sailor2 = scores.stream()
                .filter(s -> s.registrationId().equals(1L))
                .findFirst().orElseThrow();

        assertThat(sailor1.rank()).isEqualTo(1);
        assertThat(sailor1.netPoints()).isEqualTo(5);
        assertThat(sailor1.totalPoints()).isEqualTo(5);

        assertThat(sailor2.rank()).isEqualTo(2);
        assertThat(sailor2.netPoints()).isEqualTo(6);
        assertThat(sailor2.totalPoints()).isEqualTo(6);
    }

    @Test
    void calculateScores_withOneThrowout_excludesWorstRace() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 5, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 2, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 101L, 1, 3, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 102L, 2, 2, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 103L, 3, 1, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 3, 0);

        LowPointScoringCalculator.SailorScore sailor1 = scores.stream()
                .filter(s -> s.registrationId().equals(1L))
                .findFirst().orElseThrow();
        LowPointScoringCalculator.SailorScore sailor2 = scores.stream()
                .filter(s -> s.registrationId().equals(2L))
                .findFirst().orElseThrow();

        assertThat(sailor1.netPoints()).isEqualTo(3);
        assertThat(sailor1.totalPoints()).isEqualTo(8);

        LowPointScoringCalculator.RaceScore excludedRace = sailor1.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .findFirst().orElseThrow();
        assertThat(excludedRace.points()).isEqualTo(5);

        assertThat(sailor2.netPoints()).isEqualTo(3);
        assertThat(sailor2.totalPoints()).isEqualTo(6);
    }

    @Test
    void calculateScores_withTwoThrowouts_excludesTwoWorstRaces() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 10, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 104L, 4, 8, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 2, 0);

        LowPointScoringCalculator.SailorScore sailor = scores.getFirst();

        assertThat(sailor.netPoints()).isEqualTo(3);
        assertThat(sailor.totalPoints()).isEqualTo(21);

        long excludedCount = sailor.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .count();
        assertThat(excludedCount).isEqualTo(2);

        List<Integer> excludedPoints = sailor.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .map(LowPointScoringCalculator.RaceScore::points)
                .sorted()
                .toList();
        assertThat(excludedPoints).containsExactly(8, 10);
    }

    @Test
    void calculateScores_nonExcludableCircumstance_cannotBeExcluded() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 10, true),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 104L, 4, 5, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 4, 0);

        LowPointScoringCalculator.SailorScore sailor = scores.getFirst();

        assertThat(sailor.netPoints()).isEqualTo(13);
        assertThat(sailor.totalPoints()).isEqualTo(18);

        LowPointScoringCalculator.RaceScore excludedRace = sailor.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .findFirst().orElseThrow();
        assertThat(excludedRace.points()).isEqualTo(5);

        LowPointScoringCalculator.RaceScore dneRace = sailor.raceScores().stream()
                .filter(r -> r.points().equals(10))
                .findFirst().orElseThrow();
        assertThat(dneRace.excluded()).isFalse();
    }

    @Test
    void calculateScores_moreThrowoutsThanRaces_excludesAllExcludable() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 2, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 1, 0);

        LowPointScoringCalculator.SailorScore sailor = scores.getFirst();

        long excludedCount = sailor.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .count();
        assertThat(excludedCount).isEqualTo(2);
        assertThat(sailor.netPoints()).isEqualTo(0);
    }

    @Test
    void calculateScores_emptySailors_returnsEmptyList() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of();

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 1, 0);

        assertThat(scores).isEmpty();
    }

    @Test
    void calculateScores_tieBreaker_usesTotalPointsForRanking() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 5, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 101L, 1, 2, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 102L, 2, 1, false),
                new LowPointScoringCalculator.RaceResultInput(2L, 103L, 3, 3, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 3, 0);

        LowPointScoringCalculator.SailorScore sailor1 = scores.get(0);
        LowPointScoringCalculator.SailorScore sailor2 = scores.get(1);

        assertThat(sailor1.netPoints()).isEqualTo(3);
        assertThat(sailor2.netPoints()).isEqualTo(3);

        assertThat(sailor1.totalPoints()).isLessThan(sailor2.totalPoints());
        assertThat(sailor1.rank()).isEqualTo(1);
        assertThat(sailor2.rank()).isEqualTo(2);
    }

    @Test
    void calculateScores_raceScoresAreOrderedByRaceNumber() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 3, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 2, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 0, 0);

        LowPointScoringCalculator.SailorScore sailor = scores.getFirst();
        List<Integer> raceNumbers = sailor.raceScores().stream()
                .map(LowPointScoringCalculator.RaceScore::raceNumber)
                .toList();

        assertThat(raceNumbers).containsExactly(1, 2, 3);
    }

    @Test
    void calculateScores_throwoutAfterFourRaces_appliesOneThrowout() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 5, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 104L, 4, 3, false)
        );

        List<LowPointScoringCalculator.SailorScore> scores = calculator.calculateScores(results, 4, 0);

        LowPointScoringCalculator.SailorScore sailor = scores.getFirst();
        assertThat(sailor.netPoints()).isEqualTo(6);
        assertThat(sailor.totalPoints()).isEqualTo(11);

        LowPointScoringCalculator.RaceScore excludedRace = sailor.raceScores().stream()
                .filter(LowPointScoringCalculator.RaceScore::excluded)
                .findFirst().orElseThrow();
        assertThat(excludedRace.points()).isEqualTo(5);
    }

    @Test
    void calculateScores_throwoutWithLimit_respectsLimit() {
        List<LowPointScoringCalculator.RaceResultInput> results = List.of(
                new LowPointScoringCalculator.RaceResultInput(1L, 101L, 1, 5, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 102L, 2, 1, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 103L, 3, 2, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 104L, 4, 6, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 105L, 5, 3, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 106L, 6, 4, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 107L, 7, 7, false),
                new LowPointScoringCalculator.RaceResultInput(1L, 108L, 8, 8, false)
        );

        List<LowPointScoringCalculator.SailorScore> scoresUnlimited = calculator.calculateScores(results, 4, 0);
        assertThat(scoresUnlimited.getFirst().netPoints()).isEqualTo(21);

        List<LowPointScoringCalculator.SailorScore> scoresLimited = calculator.calculateScores(results, 4, 1);
        assertThat(scoresLimited.getFirst().netPoints()).isEqualTo(28);
    }
}
