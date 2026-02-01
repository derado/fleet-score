package com.fleetscore.organisation.api.dto;

import jakarta.validation.constraints.NotNull;

public record TransferOrganisationOwnerRequest(
        @NotNull(message = "User ID is required")
        Long userId
) {}
