package com.fleetscore.user.api;

import com.fleetscore.common.api.NoContent;
import com.fleetscore.user.api.dto.InvitationRequest;
import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Admin", description = "Administrative operations")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/invitations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NoContent>> invite(@AuthenticationPrincipal UserAccount currentUser,
                                                    @Valid @RequestBody InvitationRequest request,
                                                    HttpServletRequest httpRequest) {
        userService.createInvitation(request.email(), currentUser.getId());
        ApiResponse<NoContent> body = ApiResponse.ok(
                "Invitation email sent",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
