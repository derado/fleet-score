package com.fleetscore.common.events;

public record PasswordResetEmailRequested(String email, String token) {}
