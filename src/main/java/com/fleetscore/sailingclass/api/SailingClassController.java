package com.fleetscore.sailingclass.api;

import com.fleetscore.sailingclass.api.dto.SailingClassResponse;
import com.fleetscore.sailingclass.service.SailingClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Sailing Classes", description = "Sailing class reference data")
@RestController
@RequestMapping("/api/sailing-classes")
@RequiredArgsConstructor
public class SailingClassController {

    private final SailingClassService sailingClassService;

    @GetMapping
    public ResponseEntity<List<SailingClassResponse>> findAllSailingClasses() {
        return ResponseEntity.ok(sailingClassService.findAllSailingClasses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SailingClassResponse> findSailingClassById(@PathVariable Long id) {
        return ResponseEntity.ok(sailingClassService.findSailingClassById(id));
    }
}
