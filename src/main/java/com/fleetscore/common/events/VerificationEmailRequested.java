package com.fleetscore.common.events;

public record VerificationEmailRequested(String email, String token) {}
