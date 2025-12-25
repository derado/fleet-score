package com.fleetscore.organisation.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.organisation.api.dto.CreateOrganisationRequest;
import com.fleetscore.organisation.api.dto.OrganisationResponse;
import com.fleetscore.organisation.api.dto.PromoteOrganisationAdminRequest;
import com.fleetscore.organisation.service.OrganisationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.fleetscore.user.domain.UserAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganisationResponse>>> findAllOrganisations(HttpServletRequest httpRequest) {
        List<OrganisationResponse> data = organisationService.findAllOrganisations();
        ApiResponse<List<OrganisationResponse>> body = ApiResponse.ok(
                data,
                "Organisations retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{organisationId}")
    public ResponseEntity<ApiResponse<OrganisationResponse>> findOrganisationById(
            @PathVariable Long organisationId,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.findOrganisationById(organisationId);
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "Organisation retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganisationResponse>> createOrganisation(
            @AuthenticationPrincipal UserAccount currentUser,
            @Valid @RequestBody CreateOrganisationRequest request,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.createOrganisation(currentUser, request.name());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "Organisation created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{organisationId}/admins")
    @PreAuthorize("@orgAuthz.isAdmin(authentication.token.claims['email'], #organisationId)")
    public ResponseEntity<ApiResponse<OrganisationResponse>> promoteAdmin(
            @PathVariable Long organisationId,
            @Valid @RequestBody PromoteOrganisationAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.promoteAdmin(organisationId, request.userId());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "Organisation admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
