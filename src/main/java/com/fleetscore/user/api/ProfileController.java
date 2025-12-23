package com.fleetscore.user.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.user.api.dto.ProfileResponse;
import com.fleetscore.user.api.dto.UpdateProfileRequest;
import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> upsertMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = jwt.getClaimAsString("email");
        Profile profile = userService.upsertProfile(email, request.firstName(), request.lastName());
        ProfileResponse data = new ProfileResponse(email, profile.getFirstName(), profile.getLastName());
        ApiResponse<ProfileResponse> body = ApiResponse.ok(data, "Profile updated", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }
}
