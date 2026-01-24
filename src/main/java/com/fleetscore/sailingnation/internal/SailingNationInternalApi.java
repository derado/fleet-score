package com.fleetscore.sailingnation.internal;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.sailingnation.domain.SailingNation;
import com.fleetscore.sailingnation.repository.SailingNationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class SailingNationInternalApi {

    private final SailingNationRepository sailingNationRepository;

    @Transactional(readOnly = true)
    public List<SailingNation> findAll() {
        return sailingNationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SailingNation findById(Long id) {
        return sailingNationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailing nation not found"));
    }
}
