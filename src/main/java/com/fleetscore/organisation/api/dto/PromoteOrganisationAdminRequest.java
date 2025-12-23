package com.fleetscore.organisation.api.dto;

import jakarta.validation.constraints.NotNull;

public record PromoteOrganisationAdminRequest(
        @NotNull Long userId
) {}
