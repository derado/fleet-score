package com.fleetscore.organisation.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.organisation.api.dto.CreateOrganisationRequest;
import com.fleetscore.organisation.api.dto.OrganisationResponse;
import com.fleetscore.organisation.api.dto.PromoteOrganisationAdminRequest;
import com.fleetscore.organisation.api.dto.TransferOrganisationOwnerRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Organisations", description = "Organisation management")
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
                "ORGANISATIONS_RETRIEVED",
                "Organisations retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{organisationId}/admins/{adminUserId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganisationResponse>> removeAdmin(
            @PathVariable Long organisationId,
            @PathVariable Long adminUserId,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.removeAdmin(organisationId, adminUserId);
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "ORGANISATION_ADMIN_REMOVED",
                "Organisation admin removed",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{organisationId}/owner")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganisationResponse>> transferOwnership(
            @PathVariable Long organisationId,
            @Valid @RequestBody TransferOrganisationOwnerRequest request,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.transferOwnership(organisationId, request.userId());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "ORGANISATION_OWNERSHIP_TRANSFERRED",
                "Organisation owner transferred",
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
                "ORGANISATION_RETRIEVED",
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
        OrganisationResponse data = organisationService.createOrganisation(currentUser, request);
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "ORGANISATION_CREATED",
                "Organisation created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{organisationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrganisationResponse>> updateOrganisation(
            @PathVariable Long organisationId,
            @Valid @RequestBody CreateOrganisationRequest request,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.updateOrganisation(organisationId, request);
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "ORGANISATION_UPDATED",
                "Organisation updated",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{organisationId}/admins")
    public ResponseEntity<ApiResponse<OrganisationResponse>> promoteAdmin(
            @PathVariable Long organisationId,
            @Valid @RequestBody PromoteOrganisationAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        OrganisationResponse data = organisationService.promoteAdmin(organisationId, request.userId());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "ORGANISATION_ADMIN_PROMOTED",
                "Organisation admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
