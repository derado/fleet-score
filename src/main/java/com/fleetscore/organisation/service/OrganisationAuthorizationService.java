package com.fleetscore.organisation.service;

import com.fleetscore.organisation.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("orgAuthz")
@RequiredArgsConstructor
public class OrganisationAuthorizationService {

    private final OrganisationRepository organisationRepository;

    public boolean isAdmin(String email, Long organisationId) {
        if (email == null || organisationId == null) {
            return false;
        }
        return organisationRepository.existsByIdAndAdmins_Email(organisationId, email);
    }
}
