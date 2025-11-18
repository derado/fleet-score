package com.fleetscore.user.api.dto;

import com.fleetscore.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record InvitationRequest(
        @Email @NotBlank String email,
        @NotEmpty Set<Role> roles
) {}
