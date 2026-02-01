package com.fleetscore.club.service;

import com.fleetscore.club.api.dto.CreateSailingClubRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public SailingClubResponse createClub(UserAccount creator, CreateSailingClubRequest request) {
        Organisation organisation = null;

        if (request.organisationId() != null) {
            if (!organisationApi.existsById(request.organisationId())) {
                throw new ResourceNotFoundException("Organisation not found");
            }

            boolean isAdmin = organisationApi.isAdmin(request.organisationId(), creator.getId());
            if (!isAdmin) {
                throw new AccessDeniedException("Only organisation admins can create clubs for the organisation");
            }

            organisation = organisationApi.findById(request.organisationId());
        }

        SailingClub club = new SailingClub();
        applyRequest(club, request);
        club.setOrganisation(organisation);
        club.setOwner(creator);
        club.getAdmins().add(creator);

        SailingClub saved = sailingClubRepository.save(club);
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @clubAuthz.isAdmin(principal?.id, #clubId)")
    public SailingClubResponse updateClub(Long clubId, CreateSailingClubRequest request) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        Organisation organisation = null;
        if (request.organisationId() != null) {
            organisation = organisationApi.findById(request.organisationId());
        }

        applyRequest(club, request);
        club.setOrganisation(organisation);
        return toResponse(club);
    }

    private void applyRequest(SailingClub club, CreateSailingClubRequest request) {
        club.setName(request.name());
        club.setCountry(request.country());
        club.setPlace(request.place());
        club.setPostCode(request.postCode());
        club.setAddress(request.address());
        club.setEmail(request.email());
        club.setPhone(request.phone());
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @clubAuthz.isAdmin(principal?.id, #clubId)")
    public SailingClubResponse promoteAdmin(Long clubId, Long newAdminUserId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        UserAccount newAdmin = userApi.findById(newAdminUserId);

        club.getAdmins().add(newAdmin);
        return toResponse(club);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @clubAuthz.isOwner(principal?.id, #clubId)")
    public SailingClubResponse removeAdmin(Long clubId, Long adminUserId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        if (club.getOwner().getId().equals(adminUserId)) {
            throw new AccessDeniedException("Club owner cannot be removed as admin");
        }

        UserAccount admin = userApi.findById(adminUserId);
        club.getAdmins().remove(admin);
        return toResponse(club);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @clubAuthz.isOwner(principal?.id, #clubId)")
    public SailingClubResponse transferOwnership(Long clubId, Long newOwnerUserId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        UserAccount newOwner = userApi.findById(newOwnerUserId);
        if (!club.getAdmins().contains(newOwner)) {
            throw new AccessDeniedException("New owner must already be a club admin");
        }

        club.setOwner(newOwner);
        return toResponse(club);
    }

    @Transactional
    public SailingClubResponse joinClub(UserAccount user, Long clubId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        club.getMembers().add(user);
        sailingClubRepository.save(club);
        return toResponse(club);
    }

    @Transactional
    public SailingClubResponse leaveClub(UserAccount user, Long clubId) {
        SailingClub club = sailingClubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        club.getMembers().remove(user);
        sailingClubRepository.save(club);
        return toResponse(club);
    }

    private SailingClubResponse toResponse(SailingClub club) {
        Long orgId = club.getOrganisation() != null ? club.getOrganisation().getId() : null;
        String orgName = club.getOrganisation() != null ? club.getOrganisation().getName() : null;
        return new SailingClubResponse(
                club.getId(),
                club.getName(),
                club.getCountry(),
                club.getPlace(),
                club.getPostCode(),
                club.getAddress(),
                club.getEmail(),
                club.getPhone(),
                orgId,
                orgName,
                club.getOwner().getId()
        );
    }
}
