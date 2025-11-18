package com.fleetscore.user.api;

import com.fleetscore.user.api.dto.AcceptInvitationRequest;
import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.api.dto.MeResponse;
import com.fleetscore.user.api.dto.LoginRequest;
import com.fleetscore.user.api.dto.TokenResponse;
import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.user.service.UserService;
import com.fleetscore.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserAccountRepository users;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegistrationRequest request,
                                                      HttpServletRequest httpRequest) {
        userService.registerAndCreateOrganisation(request);
        ApiResponse<Void> body = ApiResponse.ok(
                "Registration successful. Verification email sent.",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(@RequestParam("token") String token,
                                                    HttpServletRequest httpRequest) {
        userService.verifyEmail(token);
        ApiResponse<Void> resp = ApiResponse.ok("Email verified. You can now log in.", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest req,
                                                             HttpServletRequest httpRequest) {
        userService.acceptInvitation(req.token(), req.password());
        ApiResponse<Void> resp = ApiResponse.ok("Invitation accepted", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) {
        TokenResponse tokens = authService.login(request, httpResponse);
        ApiResponse<TokenResponse> body = ApiResponse.ok(tokens, "Login successful", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest httpRequest,
                                                              HttpServletResponse httpResponse) {
        TokenResponse tokens = authService.refresh(httpRequest, httpResponse);
        ApiResponse<TokenResponse> body = ApiResponse.ok(tokens, "Token refreshed", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        ApiResponse<Void> body = ApiResponse.ok("Logged out", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(@AuthenticationPrincipal(expression = "claims['email']") String email,
                                                      Authentication authentication,
                                                      HttpServletRequest httpRequest) {
        if (authentication == null || !authentication.isAuthenticated() || email == null) {
            MeResponse data = new MeResponse(false, null, null, null, null);
            ApiResponse<MeResponse> body = ApiResponse.ok(data, "Anonymous", HttpStatus.OK.value(), httpRequest.getRequestURI());
            return ResponseEntity.ok(body);
        }
        UserAccount ua = users.findByEmail(email).orElse(null);
        List<String> roles = authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList();
        Boolean verified = ua != null ? ua.isEmailVerified() : null;
        Long orgId = ua != null && ua.getOrganisation() != null ? ua.getOrganisation().getId() : null;
        MeResponse data = new MeResponse(true, email, roles, verified, orgId);
        ApiResponse<MeResponse> body = ApiResponse.ok(data, "Current user", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }
}
