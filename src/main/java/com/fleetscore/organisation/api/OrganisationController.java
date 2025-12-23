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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganisationResponse>> createOrganisation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateOrganisationRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        OrganisationResponse data = organisationService.createOrganisation(email, request.name());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "Organisation created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{organisationId}/admins")
    public ResponseEntity<ApiResponse<OrganisationResponse>> promoteAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long organisationId,
            @Valid @RequestBody PromoteOrganisationAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        OrganisationResponse data = organisationService.promoteAdmin(email, organisationId, request.userId());
        ApiResponse<OrganisationResponse> body = ApiResponse.ok(
                data,
                "Organisation admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
