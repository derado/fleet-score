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
import com.fleetscore.user.domain.UserAccount;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class SailingClubController {

    private final SailingClubService sailingClubService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SailingClubResponse>>> findAllClubs(HttpServletRequest httpRequest) {
        List<SailingClubResponse> data = sailingClubService.findAllClubs();
        ApiResponse<List<SailingClubResponse>> body = ApiResponse.ok(
                data,
                "Clubs retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<SailingClubResponse>> findClubById(
            @PathVariable Long clubId,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.findClubById(clubId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Club retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SailingClubResponse>> createClub(
            @AuthenticationPrincipal UserAccount currentUser,
            @Valid @RequestBody CreateSailingClubRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.createClub(currentUser, request.name(), request.place(), request.organisationId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Sailing club created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{clubId}/admins")
    @PreAuthorize("@clubAuthz.isAdmin(authentication.token.claims['email'], #clubId)")
    public ResponseEntity<ApiResponse<SailingClubResponse>> promoteAdmin(
            @PathVariable Long clubId,
            @Valid @RequestBody PromoteClubAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.promoteAdmin(clubId, request.userId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Club admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{clubId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SailingClubResponse>> joinClub(
            @AuthenticationPrincipal UserAccount currentUser,
            @PathVariable Long clubId,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.joinClub(currentUser, clubId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Joined club",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{clubId}/members")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SailingClubResponse>> leaveClub(
            @AuthenticationPrincipal UserAccount currentUser,
            @PathVariable Long clubId,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.leaveClub(currentUser, clubId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Left club",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
