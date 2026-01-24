package com.fleetscore.sailingnation.api;

import com.fleetscore.sailingnation.api.dto.SailingNationResponse;
import com.fleetscore.sailingnation.service.SailingNationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Sailing Nations", description = "Sailing nation reference data")
@RestController
@RequestMapping("/api/sailing-nations")
@RequiredArgsConstructor
public class SailingNationController {

    private final SailingNationService sailingNationService;

    @GetMapping
    public ResponseEntity<List<SailingNationResponse>> findAllSailingNations(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String country) {
        return ResponseEntity.ok(sailingNationService.findAllSailingNations(code, country));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SailingNationResponse> findSailingNationById(@PathVariable Long id) {
        return ResponseEntity.ok(sailingNationService.findSailingNationById(id));
    }
}
