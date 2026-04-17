package com.fleetscore.user.api.dto;

public record RegisteredClubResponse(
        Long id,
        String name,
        String place,
        Long sailingNationId
) {}
