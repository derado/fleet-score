package com.fleetscore.regatta.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.regatta.api.dto.CreateRegattaRequest;
import com.fleetscore.regatta.api.dto.PromoteRegattaAdminRequest;
import com.fleetscore.regatta.api.dto.RegattaResponse;
import com.fleetscore.regatta.api.dto.UpdateRegattaRequest;
import com.fleetscore.regatta.service.RegattaService;
import com.fleetscore.user.domain.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regattas")
@RequiredArgsConstructor
public class RegattaController {

    private final RegattaService regattaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegattaResponse>>> findAllRegattas(HttpServletRequest httpRequest) {
        List<RegattaResponse> data = regattaService.findAllRegattas();
        ApiResponse<List<RegattaResponse>> body = ApiResponse.ok(
                data,
                "Regattas retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{regattaId}")
    public ResponseEntity<ApiResponse<RegattaResponse>> findRegattaById(
            @PathVariable Long regattaId,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.findRegattaById(regattaId);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "Regatta retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RegattaResponse>> createRegatta(
            @AuthenticationPrincipal UserAccount currentUser,
            @Valid @RequestBody CreateRegattaRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.createRegatta(currentUser, request);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "Regatta created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{regattaId}")
    public ResponseEntity<ApiResponse<RegattaResponse>> updateRegatta(
            @PathVariable Long regattaId,
            @Valid @RequestBody UpdateRegattaRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.updateRegatta(regattaId, request);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "Regatta updated",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{regattaId}/admins")
    public ResponseEntity<ApiResponse<RegattaResponse>> promoteAdmin(
            @PathVariable Long regattaId,
            @Valid @RequestBody PromoteRegattaAdminRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.promoteAdmin(regattaId, request.userId());
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "Regatta admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
