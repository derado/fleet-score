package com.fleetscore.regatta.api.dto;

import java.time.LocalDate;

import com.fleetscore.common.domain.Gender;

public record RegistrationResponse(
        Long id,
        String sailorName,
        String email,
        LocalDate dateOfBirth,
        Gender gender,
        Long sailorId,
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
