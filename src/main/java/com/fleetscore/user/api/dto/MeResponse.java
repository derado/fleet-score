package com.fleetscore.user.api.dto;

public record MeResponse(
        boolean authenticated,
        String email,
        Boolean emailVerified
) {}
