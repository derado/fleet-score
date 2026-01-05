package com.fleetscore.organisation.service;

import com.fleetscore.organisation.api.dto.OrganisationResponse;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.internal.UserInternalApi;
import com.fleetscore.common.exception.DuplicateResourceException;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UserInternalApi userApi;

    @Transactional(readOnly = true)
    public List<OrganisationResponse> findAllOrganisations() {
        return organisationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganisationResponse findOrganisationById(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
        return toResponse(organisation);
    }

    @Transactional
    public OrganisationResponse createOrganisation(UserAccount creator, String name) {
        if (organisationRepository.existsByName(name)) {
            throw new DuplicateResourceException("Organisation", "name", name);
        }

        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.getAdmins().add(creator);

        Organisation saved = organisationRepository.save(organisation);
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @orgAuthz.isAdmin(principal?.id, #organisationId)")
    public OrganisationResponse promoteAdmin(Long organisationId, Long newAdminUserId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        UserAccount newAdmin = userApi.findById(newAdminUserId);

        organisation.getAdmins().add(newAdmin);
        return toResponse(organisation);
    }

    private OrganisationResponse toResponse(Organisation organisation) {
        return new OrganisationResponse(organisation.getId(), organisation.getName());
    }
}
