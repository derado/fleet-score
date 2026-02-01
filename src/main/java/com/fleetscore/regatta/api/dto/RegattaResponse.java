package com.fleetscore.regatta.api.dto;

import java.time.LocalDate;
import java.util.Set;

public record RegattaResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String venue,
        String country,
        String place,
        String postCode,
        String address,
        String email,
        String phone,
        Set<SailingClassSummary> sailingClasses,
        Set<OrganiserSummary> organisers,
        OrganisationSummary organisation,
        Long ownerId
) {
    public record SailingClassSummary(Long id, String name) {}
    public record OrganiserSummary(Long id, String name) {}
    public record OrganisationSummary(Long id, String name) {}
}
