package com.fleetscore.user.api.dto;

public record MeResponse(
        boolean authenticated,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Boolean emailVerified,
        boolean profileCreated
) {}
