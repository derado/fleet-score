package com.fleetscore.user.api.dto;

import java.util.List;

public record MeResponse(
        boolean authenticated,
        String email,
        List<String> roles,
        Boolean emailVerified
) {}
