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

        UserAccount creator = userAccountRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.getAdmins().add(creator);

        Organisation saved = organisationRepository.save(organisation);
        return new OrganisationResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public OrganisationResponse promoteAdmin(String requestingAdminEmail, Long organisationId, String newAdminEmail) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        boolean isAdmin = requestingAdminEmail != null
                && organisationRepository.existsByIdAndAdmins_Email(organisationId, requestingAdminEmail);
        if (!isAdmin) {
            throw new AccessDeniedException("Only organisation admins can promote admins");
        }

        UserAccount newAdmin = userAccountRepository.findByEmail(newAdminEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        organisation.getAdmins().add(newAdmin);
        return new OrganisationResponse(organisation.getId(), organisation.getName());
    }
}
