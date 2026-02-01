package com.fleetscore.regatta.api.dto;

import jakarta.validation.constraints.NotNull;

public record TransferRegattaOwnerRequest(
        @NotNull(message = "User ID is required")
        Long userId
) {}
