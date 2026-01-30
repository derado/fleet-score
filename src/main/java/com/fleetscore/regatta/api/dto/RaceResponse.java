package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Circumstance;

import java.time.LocalDate;
import java.util.List;

public record RaceResponse(
        Long id,
        Long regattaId,
        SailingClassSummary sailingClass,
        Integer raceNumber,
        LocalDate raceDate,
        List<RaceResultItem> results
) {
    public record SailingClassSummary(Long id, String name) {}

    public record RaceResultItem(
            Long id,
            RegistrationSummary registration,
            Integer position,
            Circumstance circumstance,
            Integer points
    ) {}

    public record RegistrationSummary(
            Long id,
            String sailorName,
            Integer sailNumber,
            String sailingClassName,
            String nationCode
    ) {}
}
