package com.fleetscore.club.service;

import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.internal.UserInternalApi;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SailingClubService {

    private final SailingClubRepository sailingClubRepository;
    private final OrganisationInternalApi organisationApi;
    private final UserInternalApi userApi;

    @Transactional(readOnly = true)
    public List<SailingClubResponse> findAllClubs() {
        return sailingClubRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SailingClubResponse findClubById(Long id) {
        SailingClub club = sailingClubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
        return toResponse(club);
    }

    @Transactional
    public SailingClubResponse createClub(String creatorEmail, String name, String place, Long organisationId) {
        Organisation organisation = null;

        if (organisationId != null) {
            if (!organisationApi.existsById(organisationId)) {
                throw new ResourceNotFoundException("Organisation not found");
            }

            boolean isAdmin = organisationApi.isAdmin(organisationId, creatorEmail);
            if (!isAdmin) {
                throw new AccessDeniedException("Only organisation admins can create clubs for the organisation");
            }

            organisation = organisationApi.findById(organisationId);
        }

        UserAccount creator = userApi.findByEmail(creatorEmail);

        SailingClub club = new SailingClub();
        club.setName(name);
        club.setPlace(place);
        club.setOrganisation(organisation);
        club.getAdmins().add(creator);

        SailingClub saved = sailingClubRepository.save(club);
        return toResponse(saved);
    }

    @Transactional
    public SailingClubResponse promoteAdmin(Long clubId, Long newAdminUserId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        UserAccount newAdmin = userApi.findById(newAdminUserId);

        club.getAdmins().add(newAdmin);
        return toResponse(club);
    }

    @Transactional
    public SailingClubResponse joinClub(String email, Long clubId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        UserAccount user = userApi.findByEmail(email);

        club.getMembers().add(user);
        sailingClubRepository.save(club);
        return toResponse(club);
    }

    @Transactional
    public SailingClubResponse leaveClub(String email, Long clubId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        UserAccount user = userApi.findByEmail(email);

        club.getMembers().remove(user);
        sailingClubRepository.save(club);
        return toResponse(club);
    }

    private SailingClubResponse toResponse(SailingClub club) {
        Long orgId = club.getOrganisation() != null ? club.getOrganisation().getId() : null;
        String orgName = club.getOrganisation() != null ? club.getOrganisation().getName() : null;
        return new SailingClubResponse(club.getId(), club.getName(), club.getPlace(), orgId, orgName);
    }
}
