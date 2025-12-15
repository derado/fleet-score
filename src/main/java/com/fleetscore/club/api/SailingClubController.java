package com.fleetscore.club.api;

import com.fleetscore.club.api.dto.CreateSailingClubRequest;
import com.fleetscore.club.api.dto.SailingClubResponse;
import com.fleetscore.club.service.SailingClubService;
import com.fleetscore.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class SailingClubController {

    private final SailingClubService sailingClubService;

    @PostMapping
    public ResponseEntity<ApiResponse<SailingClubResponse>> createClub(
            @AuthenticationPrincipal(expression = "claims['email']") String email,
            @Valid @RequestBody CreateSailingClubRequest request,
            HttpServletRequest httpRequest
    ) {
        SailingClubResponse data = sailingClubService.createClub(email, request.name(), request.place(), request.organisationId());
        ApiResponse<SailingClubResponse> body = ApiResponse.ok(
                data,
                "Sailing club created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
