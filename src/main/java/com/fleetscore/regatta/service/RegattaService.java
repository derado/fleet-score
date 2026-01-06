package com.fleetscore.regatta.service;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.internal.ClubInternalApi;
import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.regatta.api.dto.RegattaFilter;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.api.dto.RegattaResponse;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegattaSpecification;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.internal.UserInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RegattaService {

    private final RegattaRepository regattaRepository;
    private final SailingClassInternalApi sailingClassApi;
    private final ClubInternalApi clubApi;
    private final OrganisationInternalApi organisationApi;
    private final UserInternalApi userApi;

    @Transactional(readOnly = true)
    public List<RegattaResponse> findAllRegattas(RegattaFilter filter) {
        return regattaRepository.findAll(RegattaSpecification.withFilter(filter)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RegattaResponse findRegattaById(Long id) {
        Regatta regatta = regattaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));
        return toResponse(regatta);
    }

    @Transactional
    public RegattaResponse createRegatta(UserAccount creator, RegattaRequest request) {
        Regatta regatta = new Regatta();
        applyRequest(regatta, request);
        regatta.getAdmins().add(creator);

        Regatta saved = regattaRepository.save(regatta);
        return toResponse(saved);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @regattaAuthz.isAdmin(principal?.id, #regattaId)")
    public RegattaResponse updateRegatta(Long regattaId, RegattaRequest request) {
        Regatta regatta = regattaRepository.findById(regattaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));

        applyRequest(regatta, request);
        return toResponse(regatta);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @regattaAuthz.isAdmin(principal?.id, #regattaId)")
    public RegattaResponse promoteAdmin(Long regattaId, Long newAdminUserId) {
        Regatta regatta = regattaRepository.findById(regattaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));

        UserAccount newAdmin = userApi.findById(newAdminUserId);

        regatta.getAdmins().add(newAdmin);
        return toResponse(regatta);
    }

    private void applyRequest(Regatta regatta, RegattaRequest request) {
        regatta.setName(request.name());
        regatta.setStartDate(request.startDate());
        regatta.setEndDate(request.endDate());
        regatta.setVenue(request.venue());

        Set<SailingClass> sailingClasses = request.sailingClassIds().stream()
                .map(sailingClassApi::findById)
                .collect(Collectors.toSet());
        regatta.setSailingClasses(sailingClasses);

        regatta.getOrganisers().clear();
        if (request.organiserClubIds() != null && !request.organiserClubIds().isEmpty()) {
            Set<SailingClub> organisers = request.organiserClubIds().stream()
                    .map(clubApi::findById)
                    .collect(Collectors.toSet());
            regatta.setOrganisers(organisers);
        }

        regatta.setOrganisation(null);
        if (request.organisationId() != null) {
            Organisation organisation = organisationApi.findById(request.organisationId());
            regatta.setOrganisation(organisation);
        }
    }

    private RegattaResponse toResponse(Regatta regatta) {
        Set<RegattaResponse.SailingClassSummary> sailingClasses = regatta.getSailingClasses().stream()
                .map(sc -> new RegattaResponse.SailingClassSummary(sc.getId(), sc.getName()))
                .collect(Collectors.toSet());

        Set<RegattaResponse.OrganiserSummary> organisers = regatta.getOrganisers().stream()
                .map(club -> new RegattaResponse.OrganiserSummary(club.getId(), club.getName()))
                .collect(Collectors.toSet());

        RegattaResponse.OrganisationSummary orgSummary = null;
        if (regatta.getOrganisation() != null) {
            orgSummary = new RegattaResponse.OrganisationSummary(
                    regatta.getOrganisation().getId(),
                    regatta.getOrganisation().getName()
            );
        }

        return new RegattaResponse(
                regatta.getId(),
                regatta.getName(),
                regatta.getStartDate(),
                regatta.getEndDate(),
                regatta.getVenue(),
                sailingClasses,
                organisers,
                orgSummary
        );
    }
}
