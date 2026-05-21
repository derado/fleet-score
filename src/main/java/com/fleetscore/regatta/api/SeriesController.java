package com.fleetscore.regatta.api;

import com.fleetscore.common.api.ApiResponse;
import com.fleetscore.regatta.api.dto.CreateSeriesRequest;
import com.fleetscore.regatta.api.dto.SeriesResponse;
import com.fleetscore.regatta.api.dto.SeriesScoreResponse;
import com.fleetscore.regatta.api.dto.SeriesScoringTypeResponse;
import com.fleetscore.regatta.domain.SeriesScoringType;
import com.fleetscore.regatta.service.SeriesScoringService;
import com.fleetscore.regatta.service.SeriesService;
import com.fleetscore.user.domain.UserAccount;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Series")
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;
    private final SeriesScoringService seriesScoringService;

    @GetMapping("/scoring-types")
    public ResponseEntity<ApiResponse<List<SeriesScoringTypeResponse>>> findScoringTypes(
            HttpServletRequest httpRequest
    ) {
        List<SeriesScoringTypeResponse> data = Arrays.stream(SeriesScoringType.values())
                .map(t -> new SeriesScoringTypeResponse(t, t.getDisplayName(), t.getDescription()))
                .toList();
        ApiResponse<List<SeriesScoringTypeResponse>> body = ApiResponse.ok(
                data,
                "SERIES_SCORING_TYPES_RETRIEVED",
                "Series scoring types retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SeriesResponse>> createSeries(
            @AuthenticationPrincipal UserAccount currentUser,
            @Valid @RequestBody CreateSeriesRequest request,
            HttpServletRequest httpRequest
    ) {
        SeriesResponse data = seriesService.createSeries(currentUser, request);
        ApiResponse<SeriesResponse> body = ApiResponse.ok(
                data,
                "SERIES_CREATED",
                "Series created",
                HttpStatus.CREATED.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SeriesResponse>>> findMySeries(
            @AuthenticationPrincipal UserAccount currentUser,
            HttpServletRequest httpRequest
    ) {
        List<SeriesResponse> data = seriesService.findAllByOwner(currentUser);
        ApiResponse<List<SeriesResponse>> body = ApiResponse.ok(
                data,
                "SERIES_RETRIEVED",
                "Series retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{seriesId}")
    public ResponseEntity<ApiResponse<SeriesResponse>> findSeriesById(
            @PathVariable Long seriesId,
            HttpServletRequest httpRequest
    ) {
        SeriesResponse data = seriesService.findSeriesById(seriesId);
        ApiResponse<SeriesResponse> body = ApiResponse.ok(
                data,
                "SERIES_RETRIEVED",
                "Series retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{seriesId}/scores/{sailingClassId}")
    public ResponseEntity<ApiResponse<SeriesScoreResponse>> calculateSeriesScores(
            @PathVariable Long seriesId,
            @PathVariable Long sailingClassId,
            HttpServletRequest httpRequest
    ) {
        SeriesScoreResponse data = seriesScoringService.calculateSeriesScores(seriesId, sailingClassId);
        ApiResponse<SeriesScoreResponse> body = ApiResponse.ok(
                data,
                "SERIES_SCORES_RETRIEVED",
                "Series scores retrieved",
                HttpStatus.OK.value(),
                httpRequest.getRequestURI()
        );
        return ResponseEntity.ok(body);
    }
}
