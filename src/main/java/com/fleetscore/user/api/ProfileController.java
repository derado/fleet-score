package com.fleetscore.user.api;

import com.fleetscore.club.internal.ClubInternalApi;
import com.fleetscore.club.internal.ClubSummary;
import com.fleetscore.club.internal.UserClubAssociation;
import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.regatta.internal.RegattaInternalApi;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.user.api.dto.MyClubResponse;
import com.fleetscore.user.api.dto.MyClubsResponse;
import com.fleetscore.user.api.dto.MySailorResponse;
import com.fleetscore.user.api.dto.ProfileResponse;
import com.fleetscore.user.api.dto.RegisteredClubResponse;
import com.fleetscore.user.api.dto.UpdateProfileRequest;
import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.fleetscore.user.domain.UserAccount;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Profile", description = "User profile management")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final RegattaInternalApi regattaApi;
    private final ClubInternalApi clubApi;

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileResponse>> upsertMyProfile(
            @AuthenticationPrincipal UserAccount currentUser,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = currentUser.getEmail();
        Profile profile = userService.upsertProfile(email, request.firstName(), request.lastName());
        ProfileResponse data = new ProfileResponse(email, profile.getFirstName(), profile.getLastName());
        ApiResponse<ProfileResponse> body = ApiResponse.ok(data, "PROFILE_UPDATED", "Profile updated", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me/sailors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MySailorResponse>>> getMySailors(
            @AuthenticationPrincipal UserAccount currentUser,
            HttpServletRequest httpRequest
    ) {
        List<MySailorResponse> data = regattaApi.findSailorsByUserId(currentUser.getId()).stream()
                .map(this::toMySailorResponse)
                .toList();
        ApiResponse<List<MySailorResponse>> body = ApiResponse.ok(
                data,
                "MY_SAILORS_RETRIEVED",
                "My sailors retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me/clubs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MyClubsResponse>> getMyClubs(
            @AuthenticationPrincipal UserAccount currentUser,
            HttpServletRequest httpRequest
    ) {
        Long userId = currentUser.getId();

        List<MyClubResponse> memberships = clubApi.findUserClubAssociations(userId).stream()
                .map(ProfileController::toMembershipResponse)
                .toList();

        Set<Long> registeredIds = regattaApi.findClubIdsByRegistrantUserId(userId);
        List<RegisteredClubResponse> registered = clubApi.findSummariesByIds(registeredIds).stream()
                .map(ProfileController::toRegisteredResponse)
                .toList();

        List<String> external = regattaApi.findExternalClubNamesByUserId(userId);

        MyClubsResponse data = new MyClubsResponse(memberships, registered, external);
        ApiResponse<MyClubsResponse> body = ApiResponse.ok(
                data, "MY_CLUBS_RETRIEVED", "My clubs retrieved",
                HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    private MySailorResponse toMySailorResponse(Sailor sailor) {
        return new MySailorResponse(
                sailor.getId(),
                sailor.getName(),
                sailor.getEmail(),
                sailor.getDateOfBirth(),
                sailor.getGender()
        );
    }

    private static MyClubResponse toMembershipResponse(UserClubAssociation assoc) {
        Set<MyClubResponse.Relationship> rels = assoc.roles().stream()
                .map(ProfileController::mapRole)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(MyClubResponse.Relationship.class)));
        return new MyClubResponse(assoc.id(), assoc.name(), assoc.place(), assoc.sailingNationId(), rels);
    }

    private static RegisteredClubResponse toRegisteredResponse(ClubSummary summary) {
        return new RegisteredClubResponse(summary.id(), summary.name(), summary.place(), summary.sailingNationId());
    }

    private static MyClubResponse.Relationship mapRole(UserClubAssociation.Role role) {
        return switch (role) {
            case OWNER -> MyClubResponse.Relationship.OWNER;
            case ADMIN -> MyClubResponse.Relationship.ADMIN;
            case MEMBER -> MyClubResponse.Relationship.MEMBER;
        };
    }
}
