package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Circumstance;

import java.util.List;

public record RegattaScoreResponse(
        Long regattaId,
        Long sailingClassId,
        String sailingClassName,
        int throwoutAfter,
        int throwoutLimit,
        int appliedThrowouts,
        List<RaceInfo> races,
        List<SailorStanding> standings
) {
    public record RaceInfo(
            Long raceId,
            Integer raceNumber
    ) {}

    public record SailorStanding(
            int rank,
            Long registrationId,
            String sailorName,
            Integer sailNumber,
            String nationCode,
            int netPoints,
            int totalPoints,
            List<RaceResult> raceResults
    ) {}

    public record RaceResult(
            Long raceId,
            Integer raceNumber,
            Integer position,
            Circumstance circumstance,
            Integer points,
            boolean excluded
    ) {}
}
