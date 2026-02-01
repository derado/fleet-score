package com.fleetscore.organisation.service;

import com.fleetscore.organisation.api.dto.CreateOrganisationRequest;
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
    public OrganisationResponse createOrganisation(UserAccount creator, CreateOrganisationRequest request) {
        if (organisationRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Organisation", "name", request.name());
        }

        Organisation organisation = new Organisation();
        applyRequest(organisation, request);
        organisation.setOwner(creator);
        organisation.getAdmins().add(creator);

        Organisation saved = organisationRepository.save(organisation);
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @orgAuthz.isAdmin(principal?.id, #organisationId)")
    public OrganisationResponse updateOrganisation(Long organisationId, CreateOrganisationRequest request) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        if (!organisation.getName().equalsIgnoreCase(request.name()) && organisationRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Organisation", "name", request.name());
        }

        applyRequest(organisation, request);
        return toResponse(organisation);
    }

    private void applyRequest(Organisation organisation, CreateOrganisationRequest request) {
        organisation.setName(request.name());
        organisation.setCountry(request.country());
        organisation.setPlace(request.place());
        organisation.setPostCode(request.postCode());
        organisation.setAddress(request.address());
        organisation.setEmail(request.email());
        organisation.setPhone(request.phone());
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

    @Transactional
    @PreAuthorize("isAuthenticated() and @orgAuthz.isOwner(principal?.id, #organisationId)")
    public OrganisationResponse removeAdmin(Long organisationId, Long adminUserId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        UserAccount admin = userApi.findById(adminUserId);

        if (organisation.getOwner().getId().equals(adminUserId)) {
            throw new IllegalArgumentException("Owner cannot be removed from admins");
        }

        if (!organisation.getAdmins().remove(admin)) {
            throw new ResourceNotFoundException("Organisation admin not found");
        }

        return toResponse(organisation);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @orgAuthz.isOwner(principal?.id, #organisationId)")
    public OrganisationResponse transferOwnership(Long organisationId, Long newOwnerUserId) {
        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));

        if (!organisationRepository.existsByIdAndAdmins_Id(organisationId, newOwnerUserId)) {
            throw new IllegalArgumentException("New owner must be an organisation admin");
        }

        UserAccount newOwner = userApi.findById(newOwnerUserId);

        organisation.setOwner(newOwner);
        organisation.getAdmins().add(newOwner);
        return toResponse(organisation);
    }

    private OrganisationResponse toResponse(Organisation organisation) {
        Long ownerId = organisation.getOwner() != null ? organisation.getOwner().getId() : null;
        return new OrganisationResponse(
                organisation.getId(),
                organisation.getName(),
                organisation.getCountry(),
                organisation.getPlace(),
                organisation.getPostCode(),
                organisation.getAddress(),
                organisation.getEmail(),
                organisation.getPhone(),
                ownerId
        );
    }
}
