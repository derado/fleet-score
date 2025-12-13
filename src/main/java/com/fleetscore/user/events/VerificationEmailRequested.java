package com.fleetscore.user.events;

public record VerificationEmailRequested(String email, String token) {}
