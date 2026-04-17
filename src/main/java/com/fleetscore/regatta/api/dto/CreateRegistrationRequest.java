package com.fleetscore.regatta.api.dto;

import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.validation.SailingClubIdentified;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@SailingClubIdentified
public record CreateRegistrationRequest(
    @NotBlank @Size(max = 100) String sailorName,
    @NotBlank @Email @Size(max = 255) String email,
    @NotNull @Past LocalDate dateOfBirth,
    @NotNull Gender gender,
    @Size(max = 100) String sailingClubName,
    Long sailingClubId,
    Long sailorId,
    @NotNull Long sailingClassId,
    @NotNull Long sailingNationId,
    @NotNull @Positive Integer sailNumber
) {}
