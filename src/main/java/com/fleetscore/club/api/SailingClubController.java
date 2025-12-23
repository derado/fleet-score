package com.fleetscore.club.api;

import com.fleetscore.club.api.dto.CreateSailingClubRequest;
import com.fleetscore.club.api.dto.PromoteClubAdminRequest;
import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.service.SailingClubService;
import com.fleetscore.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class SailingClubController {

    private final SailingClubService sailingClubService;

    @PostMapping
    public ResponseEntity<ApiResponse<SailingClubResponse>> createClub(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateSailingClubRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        SailingClubResponse data = sailingClubService.createClub(email, request.name(), request.place(), request.organisationId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Sailing club created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{clubId}/admins")
    public ResponseEntity<ApiResponse<SailingClubResponse>> promoteAdmin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long clubId,
            @Valid @RequestBody PromoteClubAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        SailingClubResponse data = sailingClubService.promoteAdmin(email, clubId, request.userId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Club admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse<SailingClubResponse>> joinClub(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long clubId,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        SailingClubResponse data = sailingClubService.joinClub(email, clubId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Joined club",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse<SailingClubResponse>> leaveClub(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long clubId,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        SailingClubResponse data = sailingClubService.leaveClub(email, clubId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Left club",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
