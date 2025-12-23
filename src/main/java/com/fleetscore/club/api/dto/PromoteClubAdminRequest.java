package com.fleetscore.club.api.dto;

import jakarta.validation.constraints.NotNull;

public record PromoteClubAdminRequest(
        @NotNull(message = "User ID is required")
        Long userId
) {}
