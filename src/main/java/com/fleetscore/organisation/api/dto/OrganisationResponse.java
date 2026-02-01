package com.fleetscore.organisation.api.dto;

public record OrganisationResponse(
        Long id,
        String name,
        String country,
        String place,
        String postCode,
        String address,
        String email,
        String phone,
        Long ownerId
) {}
