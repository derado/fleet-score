package com.fleetscore.regatta.api.dto;

import java.time.LocalDate;

import com.fleetscore.common.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRegistrationRequest(
        @NotBlank @Size(max = 100) String sailorName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        @NotBlank @Size(max = 100) String sailingClubName,
        Long sailingClubId,
        Long sailorId,
        @NotNull Long sailingClassId,
        @NotNull Long sailingNationId,
        @NotNull @Positive Integer sailNumber
) {}
