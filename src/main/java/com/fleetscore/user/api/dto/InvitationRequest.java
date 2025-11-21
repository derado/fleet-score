package com.fleetscore.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InvitationRequest(
        @Email @NotBlank String email
) {}
