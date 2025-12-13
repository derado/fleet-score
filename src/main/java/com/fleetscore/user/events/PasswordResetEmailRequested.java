package com.fleetscore.user.events;

public record PasswordResetEmailRequested(String email, String token) {}
