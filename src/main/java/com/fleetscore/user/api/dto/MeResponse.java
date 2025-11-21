package com.fleetscore.user.api.dto;

public record MeResponse(
        boolean authenticated,
        String email,
        String firstName,
        String lastName,
        Boolean emailVerified
) {}
