package com.fleetscore.sailor.api;

import java.util.List;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.sailor.api.dto.SailorFilter;
import com.fleetscore.sailor.api.dto.SailorRequest;
import com.fleetscore.sailor.api.dto.SailorResponse;
import com.fleetscore.sailor.service.SailorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sailors", description = "Sailor management")
@RestController
@RequestMapping("/api/sailors")
@RequiredArgsConstructor
public class SailorController {

    private final SailorService sailorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SailorResponse>>> findAllSailors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Gender gender,
            HttpServletRequest httpRequest
    ) {
        SailorFilter filter = new SailorFilter(name, email, gender);
        List<SailorResponse> data = sailorService.findAllSailors(filter);
        ApiResponse<List<SailorResponse>> body = ApiResponse.ok(
                data,
                "Sailors retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{sailorId}")
    public ResponseEntity<ApiResponse<SailorResponse>> findSailorById(
            @PathVariable Long sailorId,
            HttpServletRequest httpRequest
    ) {
        SailorResponse data = sailorService.findSailorById(sailorId);
        ApiResponse<SailorResponse> body = ApiResponse.ok(
                data,
                "Sailor retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SailorResponse>> createSailor(
            @Valid @RequestBody SailorRequest request,
            HttpServletRequest httpRequest
    ) {
        SailorResponse data = sailorService.createSailor(request);
        ApiResponse<SailorResponse> body = ApiResponse.ok(
                data,
                "Sailor created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{sailorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SailorResponse>> updateSailor(
            @PathVariable Long sailorId,
            @Valid @RequestBody SailorRequest request,
            HttpServletRequest httpRequest
    ) {
        SailorResponse data = sailorService.updateSailor(sailorId, request);
        ApiResponse<SailorResponse> body = ApiResponse.ok(
                data,
                "Sailor updated",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
