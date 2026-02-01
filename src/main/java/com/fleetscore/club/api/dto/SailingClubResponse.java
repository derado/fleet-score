package com.fleetscore.club.api.dto;

public record SailingClubResponse(
        Long id,
        String name,
        String place,
        Long organisationId,
        String organisationName,
        Long ownerId
) {}
