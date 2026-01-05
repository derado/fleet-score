package com.fleetscore.regatta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record UpdateRegattaRequest(
        @NotBlank @Size(max = 200) String name,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank @Size(max = 200) String venue,
        @NotEmpty Set<Long> sailingClassIds,
        Set<Long> organiserClubIds,
        Long organisationId
) {
}
