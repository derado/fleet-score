package com.fleetscore.regatta.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record RegattaRequest(
        @NotBlank
        @Size(max = 200)
        String name,
        @NotNull
        LocalDate startDate,
        @NotNull
        LocalDate endDate,
        @NotBlank
        @Size(max = 200)
        String venue,
        @Size(max = 100)
        String country,
        @Size(max = 100)
        String place,
        @Size(max = 20)
        String postCode,
        @Size(max = 200)
        String address,
        @Size(max = 100)
        @Email(message = "Invalid email format")
        String email,
        @Size(max = 50)
        String phone,
        @NotEmpty
        Set<Long> sailingClassIds,
        Set<Long> organiserClubIds,
        Long organisationId
) {
}
