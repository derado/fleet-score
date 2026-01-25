package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Gender;

public record RegistrationResponse(
        Long id,
        String sailorName,
        Integer yearOfBirth,
        Gender gender,
        String sailingClubName,
        Long sailingClubId,
        Long userId,
        SailingClassSummary sailingClass,
        NationSummary sailingNation,
        Integer sailNumber
) {
    public record SailingClassSummary(Long id, String name) {}
    public record NationSummary(Long id, String code, String country) {}
}
