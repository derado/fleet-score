package com.fleetscore.user.api.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiresAt) {}
