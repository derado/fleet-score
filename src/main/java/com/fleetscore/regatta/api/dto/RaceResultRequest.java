package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.Circumstance;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RaceResultRequest(
        @NotNull(message = "Registration ID is required")
        Long registrationId,

        @Positive(message = "Position must be positive")
        Integer position,

        Circumstance circumstance
) {}
