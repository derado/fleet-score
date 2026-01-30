package com.fleetscore.sailor.api.dto;

import java.time.LocalDate;

import com.fleetscore.common.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record SailorRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender
) {}
