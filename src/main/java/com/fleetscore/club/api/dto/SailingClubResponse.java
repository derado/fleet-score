package com.fleetscore.club.api.dto;

public record SailingClubResponse(
        Long id,
        String name,
        String country,
        String place,
        String postCode,
        String address,
        String email,
        String phone,
        Long organisationId,
        String organisationName,
        Long ownerId
) {}
