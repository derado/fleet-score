package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.SeriesScoringType;

import java.util.List;

public record SeriesScoreResponse(
        Long seriesId,
        String seriesName,
        SeriesScoringType scoringType,
        Long sailingClassId,
        String sailingClassName,
        int throwoutAfter,
        int throwoutLimit,
        List<RegattaInfo> regattas,
        List<SailorStanding> standings
) {
    public record RegattaInfo(Long regattaId, String regattaName) {}

    public record SailorStanding(
            int rank,
            String sailorKey,
            String sailorName,
            String nationCode,
            String clubName,
            List<RegattaScore> regattaScores,
            List<RaceScore> raceScores,
            double totalScore,
            double netScore
    ) {}

    public record RegattaScore(Long regattaId, double score, boolean excluded) {}

    public record RaceScore(Long raceId, Integer raceNumber, Long regattaId, Integer points, boolean excluded) {}
}
