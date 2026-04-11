package com.fleetscore.user.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.regatta.internal.RegattaInternalApi;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.user.api.dto.MySailorResponse;
import com.fleetscore.user.api.dto.ProfileResponse;
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

import java.util.List;

@Tag(name = "Profile", description = "User profile management")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final RegattaInternalApi regattaApi;

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

    private MySailorResponse toMySailorResponse(Sailor sailor) {
        return new MySailorResponse(
                sailor.getId(),
                sailor.getName(),
                sailor.getEmail(),
                sailor.getDateOfBirth(),
                sailor.getGender()
        );
    }
}
