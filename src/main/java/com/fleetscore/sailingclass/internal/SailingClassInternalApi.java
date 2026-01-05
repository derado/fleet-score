package com.fleetscore.sailingclass.internal;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.repository.SailingClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class SailingClassInternalApi {

    private final SailingClassRepository sailingClassRepository;

    @Transactional(readOnly = true)
    public List<SailingClass> findAll() {
        return sailingClassRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SailingClass findById(Long id) {
        return sailingClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailing class not found"));
    }
}
