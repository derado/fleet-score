package com.fleetscore.club.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSailingClubRequest(
        @NotBlank(message = "Club name is required")
        @Size(max = 200, message = "Club name must not exceed 200 characters")
        String name,

        @NotBlank(message = "Place is required")
        @Size(max = 200, message = "Place must not exceed 200 characters")
        String place,

        Long organisationId
) {}
