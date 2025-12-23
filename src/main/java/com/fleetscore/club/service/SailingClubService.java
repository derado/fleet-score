package com.fleetscore.club.service;

import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
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
public class SailingClubService {

    private final SailingClubRepository sailingClubRepository;
    private final OrganisationRepository organisationRepository;
    private final UserAccountRepository userAccountRepository;

    @Transactional
    public SailingClubResponse createClub(String creatorEmail, String name, String place, Long organisationId) {
        Organisation organisation = null;

        if (organisationId != null) {
            if (!organisationRepository.existsById(organisationId)) {
                throw new EntityNotFoundException("Organisation not found");
            }

            boolean isAdmin = organisationRepository.existsByIdAndAdmins_Email(organisationId, creatorEmail);
            if (!isAdmin) {
                throw new AccessDeniedException("Only organisation admins can create clubs for the organisation");
            }

            organisation = organisationRepository.findById(organisationId).orElseThrow();
        }

        UserAccount creator = userAccountRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SailingClub club = new SailingClub();
        club.setName(name);
        club.setPlace(place);
        club.setOrganisation(organisation);
        club.getAdmins().add(creator);

        SailingClub saved = sailingClubRepository.save(club);

        Long orgId = saved.getOrganisation() != null ? saved.getOrganisation().getId() : null;
        String orgName = saved.getOrganisation() != null ? saved.getOrganisation().getName() : null;

        return new SailingClubResponse(saved.getId(), saved.getName(), saved.getPlace(), orgId, orgName);
    }

    @Transactional
    public SailingClubResponse promoteAdmin(String requestingAdminEmail, Long clubId, Long newAdminUserId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new EntityNotFoundException("Club not found"));

        boolean isAdmin = requestingAdminEmail != null
                && sailingClubRepository.existsByIdAndAdmins_Email(clubId, requestingAdminEmail);
        if (!isAdmin) {
            throw new AccessDeniedException("Only club admins can promote admins");
        }

        UserAccount newAdmin = userAccountRepository.findById(newAdminUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        club.getAdmins().add(newAdmin);

        Long orgId = club.getOrganisation() != null ? club.getOrganisation().getId() : null;
        String orgName = club.getOrganisation() != null ? club.getOrganisation().getName() : null;

        return new SailingClubResponse(club.getId(), club.getName(), club.getPlace(), orgId, orgName);
    }
}
