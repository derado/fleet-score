package com.fleetscore.club.internal;

public record ClubSummary(
        Long id,
        String name,
        String place,
        Long sailingNationId
) {}
