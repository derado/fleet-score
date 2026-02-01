package com.fleetscore.club.api;

import com.fleetscore.club.api.dto.CreateSailingClubRequest;
import com.fleetscore.club.api.dto.PromoteClubAdminRequest;
import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.api.dto.TransferClubOwnerRequest;
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
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Clubs", description = "Sailing club management")
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
                "CLUBS_RETRIEVED",
                "Clubs retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{clubId}/admins/{adminUserId}")
    @PreAuthorize("isAuthenticated() and @clubAuthz.isOwner(principal?.id, #clubId)")
    public ResponseEntity<ApiResponse<SailingClubResponse>> removeAdmin(
            @PathVariable Long clubId,
            @PathVariable Long adminUserId,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.removeAdmin(clubId, adminUserId);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "CLUB_ADMIN_REMOVED",
                "Club admin removed",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{clubId}/owner")
    @PreAuthorize("isAuthenticated() and @clubAuthz.isOwner(principal?.id, #clubId)")
    public ResponseEntity<ApiResponse<SailingClubResponse>> transferOwnership(
            @PathVariable Long clubId,
            @Valid @RequestBody TransferClubOwnerRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.transferOwnership(clubId, request.userId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "CLUB_OWNERSHIP_TRANSFERRED",
                "Club ownership transferred",
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
                "CLUB_RETRIEVED",
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
        SailingClubResponse data = sailingClubService.createClub(currentUser, request);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "CLUB_CREATED",
                "Sailing club created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{clubId}")
    public ResponseEntity<ApiResponse<SailingClubResponse>> updateClub(
            @PathVariable Long clubId,
            @Valid @RequestBody CreateSailingClubRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.updateClub(clubId, request);
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "CLUB_UPDATED",
                "Sailing club updated",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{clubId}/admins")
    public ResponseEntity<ApiResponse<SailingClubResponse>> promoteAdmin(
            @PathVariable Long clubId,
            @Valid @RequestBody PromoteClubAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.promoteAdmin(clubId, request.userId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "CLUB_ADMIN_PROMOTED",
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
                "CLUB_JOINED",
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
                "CLUB_LEFT",
                "Left club",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
