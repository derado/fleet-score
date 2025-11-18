package com.fleetscore.user.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
        @NotBlank String token,
        @NotBlank String password
) {}
