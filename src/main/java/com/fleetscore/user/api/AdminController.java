package com.fleetscore.user.api;

import com.fleetscore.user.api.dto.InvitationRequest;
import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final UserAccountRepository users;

    @PostMapping("/invitations")
    public ResponseEntity<ApiResponse<Void>> invite(@AuthenticationPrincipal UserDetails principal,
                                                    @Valid @RequestBody InvitationRequest request,
                                                    HttpServletRequest httpRequest) {
        UserAccount admin = users.findByEmail(principal.getUsername()).orElseThrow();
        userService.createInvitation(request.email(), admin.getId());
        ApiResponse<Void> body = ApiResponse.ok(
                "Invitation email sent",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
