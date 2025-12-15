package com.fleetscore.club.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSailingClubRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 200) String place,
        Long organisationId
) {}
