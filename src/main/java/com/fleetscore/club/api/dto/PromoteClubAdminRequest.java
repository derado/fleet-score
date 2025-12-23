package com.fleetscore.club.api.dto;

import jakarta.validation.constraints.NotNull;

public record PromoteClubAdminRequest(
        @NotNull Long userId
) {}
