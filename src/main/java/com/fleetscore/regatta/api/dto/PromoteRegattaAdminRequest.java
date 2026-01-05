package com.fleetscore.regatta.api.dto;

import jakarta.validation.constraints.NotNull;

public record PromoteRegattaAdminRequest(
        @NotNull Long userId
) {
}
