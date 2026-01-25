package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRegistrationRequest(
        @NotBlank @Size(max = 100) String sailorName,
        @NotNull Integer yearOfBirth,
        @NotNull Gender gender,
        @NotBlank @Size(max = 100) String sailingClubName,
        Long sailingClubId,
        @NotNull Long sailingClassId,
        @NotNull Long sailingNationId,
        @NotNull @Positive Integer sailNumber
) {}
