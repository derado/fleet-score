package com.fleetscore.organisation.api.dto;

public record OrganisationResponse(
        Long id,
        String name,
        String ownerEmail
) {}
