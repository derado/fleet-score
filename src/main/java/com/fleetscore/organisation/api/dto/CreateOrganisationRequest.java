package com.fleetscore.organisation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganisationRequest(
        @NotBlank @Size(max = 200) String name
) {}
