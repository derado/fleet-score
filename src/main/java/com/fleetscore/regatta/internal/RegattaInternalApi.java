package com.fleetscore.regatta.internal;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.repository.RegattaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class RegattaInternalApi {

    private final RegattaRepository regattaRepository;

    @Transactional(readOnly = true)
    public List<Regatta> findAll() {
        return regattaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Regatta findById(Long id) {
        return regattaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));
    }
}
