package com.fleetscore.organisation.service;

import com.fleetscore.organisation.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganisationAuthorizationService {

    private final OrganisationRepository organisationRepository;

    public boolean isAdmin(Long userId, Long organisationId) {
        if (userId == null || organisationId == null) {
            return false;
        }
        return organisationRepository.existsByIdAndAdmins_Id(organisationId, userId);
    }
}
