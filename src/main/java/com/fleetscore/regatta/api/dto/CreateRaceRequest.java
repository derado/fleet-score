package com.fleetscore.regatta.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record CreateRaceRequest(
        @NotNull(message = "Sailing class ID is required")
        Long sailingClassId,

        @NotNull(message = "Race number is required")
        @Positive(message = "Race number must be positive")
        Integer raceNumber,

        LocalDate raceDate,

        @NotEmpty(message = "At least one result is required")
        @Valid
        List<RaceResultRequest> results
) {}
