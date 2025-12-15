package com.fleetscore.club.service;

import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.repository.OrganisationRepository;
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

        SailingClub club = new SailingClub();
        club.setName(name);
        club.setPlace(place);
        club.setOrganisation(organisation);

        SailingClub saved = sailingClubRepository.save(club);

        Long orgId = saved.getOrganisation() != null ? saved.getOrganisation().getId() : null;
        String orgName = saved.getOrganisation() != null ? saved.getOrganisation().getName() : null;

        return new SailingClubResponse(saved.getId(), saved.getName(), saved.getPlace(), orgId, orgName);
    }
}
