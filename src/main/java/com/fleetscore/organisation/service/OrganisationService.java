package com.fleetscore.organisation.service;

import com.fleetscore.organisation.api.dto.OrganisationResponse;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public OrganisationResponse createOrganisation(String creatorEmail, String name) {
        if (organisationRepository.existsByName(name)) {
            throw new IllegalStateException("Organisation name already in use");
        }

        UserAccount owner = userAccountRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setOwner(owner);
        organisation.getAdmins().add(owner);

        Organisation saved = organisationRepository.save(organisation);
        return new OrganisationResponse(saved.getId(), saved.getName(), owner.getEmail());
    }

    @Transactional
    public OrganisationResponse promoteAdmin(String ownerEmail, Long organisationId, String newAdminEmail) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        if (!organisation.getOwner().getEmail().equals(ownerEmail)) {
            throw new AccessDeniedException("Only the organisation owner can promote admins");
        }

        UserAccount newAdmin = userAccountRepository.findByEmail(newAdminEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        organisation.getAdmins().add(newAdmin);
        Organisation saved = organisationRepository.save(organisation);
        return new OrganisationResponse(saved.getId(), saved.getName(), saved.getOwner().getEmail());
    }
}
