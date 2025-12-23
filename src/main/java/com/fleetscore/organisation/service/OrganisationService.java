package com.fleetscore.organisation.service;

import com.fleetscore.organisation.api.dto.OrganisationResponse;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.common.exception.DuplicateResourceException;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UserAccountRepository userAccountRepository;

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
    public OrganisationResponse createOrganisation(String creatorEmail, String name) {
        if (organisationRepository.existsByName(name)) {
            throw new DuplicateResourceException("Organisation", "name", name);
        }

        UserAccount creator = userAccountRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.getAdmins().add(creator);

        Organisation saved = organisationRepository.save(organisation);
        return toResponse(saved);
    }

    @Transactional
    public OrganisationResponse promoteAdmin(Long organisationId, Long newAdminUserId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        UserAccount newAdmin = userAccountRepository.findById(newAdminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        organisation.getAdmins().add(newAdmin);
        return toResponse(organisation);
    }

    private OrganisationResponse toResponse(Organisation organisation) {
        return new OrganisationResponse(organisation.getId(), organisation.getName());
    }
}
