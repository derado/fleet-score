package com.fleetscore.user.api.dto;

public record ProfileResponse(
        String email,
        String firstName,
        String lastName
) {}
