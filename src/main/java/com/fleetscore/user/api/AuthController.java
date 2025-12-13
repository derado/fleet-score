package com.fleetscore.user.api;

import com.fleetscore.common.api.NoContent;
import com.fleetscore.user.api.dto.AcceptInvitationRequest;
import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.api.dto.MeResponse;
import com.fleetscore.user.api.dto.LoginRequest;
import com.fleetscore.user.api.dto.TokenResponse;
import com.fleetscore.user.api.dto.ForgotPasswordRequest;
import com.fleetscore.user.api.dto.ResetPasswordRequest;
import com.fleetscore.user.api.dto.ResendVerificationRequest;
import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.ProfileRepository;
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


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserAccountRepository users;
    private final ProfileRepository profiles;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<NoContent>> register(@Valid @RequestBody RegistrationRequest request,
                                                   HttpServletRequest httpRequest) {
        userService.registerUser(request);
        ApiResponse<NoContent> body = ApiResponse.ok(
                "Registration successful. Verification email sent.",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<NoContent>> verify(@RequestParam("token") String token,
                                                 HttpServletRequest httpRequest) {
        userService.verifyEmail(token);
        ApiResponse<NoContent> resp = ApiResponse.ok(
                "Email verified. You can now log in.",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<NoContent>> resendVerification(@Valid @RequestBody ResendVerificationRequest req,
                                                             HttpServletRequest httpRequest) {
        userService.resendVerificationEmail(req.email());
        ApiResponse<NoContent> resp = ApiResponse.ok(
                "If the email is registered and not verified, a new verification email has been sent.",
                HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/accept-invitation")
    public ResponseEntity<ApiResponse<NoContent>> acceptInvitation(@Valid @RequestBody AcceptInvitationRequest req,
                                                          HttpServletRequest httpRequest) {
        userService.acceptInvitation(req.token(), req.password());
        ApiResponse<NoContent> resp = ApiResponse.ok("Invitation accepted", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<NoContent>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req,
                                                         HttpServletRequest httpRequest) {
        userService.requestPasswordReset(req.email());
        ApiResponse<NoContent> resp = ApiResponse.ok(
                "If an account exists for this email, a password reset email has been sent.",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<NoContent>> resetPassword(@Valid @RequestBody ResetPasswordRequest req,
                                                        HttpServletRequest httpRequest) {
        userService.resetPassword(req.token(), req.password());
        ApiResponse<NoContent> resp = ApiResponse.ok("Password reset successful", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) {
        TokenResponse tokens = authService.login(request, httpResponse);
        ApiResponse<TokenResponse> body = ApiResponse.ok(tokens, "Login successful", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(HttpServletRequest httpRequest,
                                                              HttpServletResponse httpResponse) {
        TokenResponse tokens = authService.refresh(httpRequest, httpResponse);
        ApiResponse<TokenResponse> body = ApiResponse.ok(tokens, "Token refreshed", HttpStatus.OK.value(),
                httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<NoContent>> logout(HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        ApiResponse<NoContent> body = ApiResponse.ok("Logged out", HttpStatus.OK.value(), httpRequest.getRequestURI());
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
        Boolean verified = ua != null ? ua.isEmailVerified() : null;
        String firstName = null;
        String lastName = null;

        if (ua != null) {
            Profile profile = profiles.findByUser(ua).orElse(null);
            if (profile != null) {
                firstName = profile.getFirstName();
                lastName = profile.getLastName();
            }
        }

        MeResponse data = new MeResponse(true, email, firstName, lastName, verified);
        ApiResponse<MeResponse> body = ApiResponse.ok(data, "Current user", HttpStatus.OK.value(), httpRequest.getRequestURI());
        return ResponseEntity.ok(body);
    }
}
