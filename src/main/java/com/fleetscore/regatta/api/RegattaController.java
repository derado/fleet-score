package com.fleetscore.regatta.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.regatta.api.dto.CreateRaceRequest;
import com.fleetscore.regatta.api.dto.CreateRegistrationRequest;
import com.fleetscore.regatta.api.dto.PromoteRegattaAdminRequest;
import com.fleetscore.regatta.api.dto.RaceResponse;
import com.fleetscore.regatta.api.dto.RegattaFilter;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.api.dto.RegattaResponse;
import com.fleetscore.regatta.api.dto.RegistrationResponse;
import com.fleetscore.regatta.api.dto.TransferRegattaOwnerRequest;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.service.RaceService;
import com.fleetscore.regatta.service.RegattaService;
import com.fleetscore.regatta.service.RegistrationService;
import com.fleetscore.user.domain.UserAccount;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Regattas", description = "Regatta management")
@RestController
@RequestMapping("/api/regattas")
@RequiredArgsConstructor
public class RegattaController {

    private final RegattaService regattaService;
    private final RegistrationService registrationService;
    private final RaceService raceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RegattaResponse>>> findAllRegattas(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) String venue,
            @RequestParam(required = false) String sailingClass,
            @RequestParam(required = false) String organiser,
            @RequestParam(required = false) String organisation,
            HttpServletRequest httpRequest
    ) {
        RegattaFilter filter = new RegattaFilter(name, startDate, venue, sailingClass, organiser, organisation);
        List<RegattaResponse> data = regattaService.findAllRegattas(filter);
        ApiResponse<List<RegattaResponse>> body = ApiResponse.ok(
                data,
                "REGATTAS_RETRIEVED",
                "Regattas retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{regattaId}/admins/{adminUserId}")
    public ResponseEntity<ApiResponse<RegattaResponse>> removeAdmin(
            @PathVariable Long regattaId,
            @PathVariable Long adminUserId,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.removeAdmin(regattaId, adminUserId);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "REGATTA_ADMIN_REMOVED",
                "Regatta admin removed",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{regattaId}/owner")
    public ResponseEntity<ApiResponse<RegattaResponse>> transferOwnership(
            @PathVariable Long regattaId,
            @Valid @RequestBody TransferRegattaOwnerRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.transferOwnership(regattaId, request.userId());
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "REGATTA_OWNERSHIP_TRANSFERRED",
                "Regatta ownership transferred",
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
                "REGATTA_RETRIEVED",
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
            @Valid @RequestBody RegattaRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.createRegatta(currentUser, request);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "REGATTA_CREATED",
                "Regatta created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{regattaId}")
    public ResponseEntity<ApiResponse<RegattaResponse>> updateRegatta(
            @PathVariable Long regattaId,
            @Valid @RequestBody RegattaRequest request,
            HttpServletRequest httpRequest
    ) {
        RegattaResponse data = regattaService.updateRegatta(regattaId, request);
        ApiResponse<RegattaResponse> body = ApiResponse.ok(
                data,
                "REGATTA_UPDATED",
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
                "REGATTA_ADMIN_PROMOTED",
                "Regatta admin promoted",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{regattaId}/registrations")
    public ResponseEntity<ApiResponse<RegistrationResponse>> createRegistration(
            @AuthenticationPrincipal UserAccount currentUser,
            @PathVariable Long regattaId,
            @Valid @RequestBody CreateRegistrationRequest request,
            HttpServletRequest httpRequest
    ) {
        RegistrationResponse data = registrationService.createRegistration(regattaId, request, currentUser);
        ApiResponse<RegistrationResponse> body = ApiResponse.ok(
                data,
                "REGISTRATION_CREATED",
                "Registration created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{regattaId}/registrations")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> findRegistrations(
            @PathVariable Long regattaId,
            @RequestParam(required = false) Long sailingClassId,
            @RequestParam(required = false) Long sailingNationId,
            @RequestParam(required = false) String sailorName,
            @RequestParam(required = false) String sailingClubName,
            @RequestParam(required = false) Integer sailNumber,
            @RequestParam(required = false) Gender gender,
            HttpServletRequest httpRequest
    ) {
        List<RegistrationResponse> data = registrationService.findRegistrationsByRegatta(
                regattaId, sailingClassId, sailingNationId, sailorName, sailingClubName, sailNumber, gender);
        ApiResponse<List<RegistrationResponse>> body = ApiResponse.ok(
                data,
                "REGISTRATIONS_RETRIEVED",
                "Registrations retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{regattaId}/races")
    public ResponseEntity<ApiResponse<RaceResponse>> createRace(
            @PathVariable Long regattaId,
            @Valid @RequestBody CreateRaceRequest request,
            HttpServletRequest httpRequest
    ) {
        RaceResponse data = raceService.createRace(regattaId, request);
        ApiResponse<RaceResponse> body = ApiResponse.ok(
                data,
                "RACE_CREATED",
                "Race created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{regattaId}/races")
    public ResponseEntity<ApiResponse<List<RaceResponse>>> findRaces(
            @PathVariable Long regattaId,
            @RequestParam(required = false) Long sailingClassId,
            HttpServletRequest httpRequest
    ) {
        List<RaceResponse> data = raceService.findRacesByRegatta(regattaId, sailingClassId);
        ApiResponse<List<RaceResponse>> body = ApiResponse.ok(
                data,
                "RACES_RETRIEVED",
                "Races retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{regattaId}/races/{raceId}")
    public ResponseEntity<ApiResponse<RaceResponse>> findRaceById(
            @PathVariable Long regattaId,
            @PathVariable Long raceId,
            HttpServletRequest httpRequest
    ) {
        RaceResponse data = raceService.findRaceById(raceId);
        ApiResponse<RaceResponse> body = ApiResponse.ok(
                data,
                "RACE_RETRIEVED",
                "Race retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{regattaId}/races/{raceId}")
    public ResponseEntity<ApiResponse<RaceResponse>> updateRace(
            @PathVariable Long regattaId,
            @PathVariable Long raceId,
            @Valid @RequestBody CreateRaceRequest request,
            HttpServletRequest httpRequest
    ) {
        RaceResponse data = raceService.updateRace(raceId, request);
        ApiResponse<RaceResponse> body = ApiResponse.ok(
                data,
                "RACE_UPDATED",
                "Race updated",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
