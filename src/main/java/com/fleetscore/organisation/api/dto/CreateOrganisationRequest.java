package com.fleetscore.organisation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest(
        @NotBlank(message = "Organisation name is required")
        @Size(max = 200, message = "Organisation name must not exceed 200 characters")
        String name
) {}
