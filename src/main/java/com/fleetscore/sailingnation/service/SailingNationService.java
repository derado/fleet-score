package com.fleetscore.sailingnation.service;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.sailingnation.api.dto.SailingNationResponse;
import com.fleetscore.sailingnation.domain.SailingNation;
import com.fleetscore.sailingnation.repository.SailingNationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class SailingNationService {

    private final SailingNationRepository sailingNationRepository;

    @Transactional(readOnly = true)
    public List<SailingNationResponse> findAllSailingNations(String code, String country) {
        return sailingNationRepository.findAllWithFilters(code, country).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SailingNationResponse findSailingNationById(Long id) {
        SailingNation sailingNation = sailingNationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailing nation not found"));
        return toResponse(sailingNation);
    }

    private SailingNationResponse toResponse(SailingNation sailingNation) {
        return new SailingNationResponse(
                sailingNation.getId(),
                sailingNation.getCode(),
                sailingNation.getCountry()
        );
    }
}
