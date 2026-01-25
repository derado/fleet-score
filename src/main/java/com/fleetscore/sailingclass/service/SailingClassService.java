package com.fleetscore.sailingclass.service;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.sailingclass.api.dto.SailingClassResponse;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.repository.SailingClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class SailingClassService {

    private final SailingClassRepository sailingClassRepository;

    @Transactional(readOnly = true)
    public List<SailingClassResponse> findAllSailingClasses(String name, String classCode, String hullType, String worldSailingStatus) {
        return sailingClassRepository.findAllWithFilters(name, classCode, hullType, worldSailingStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SailingClassResponse findSailingClassById(Long id) {
        SailingClass sailingClass = sailingClassRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailing class not found"));
        return toResponse(sailingClass);
    }

    private SailingClassResponse toResponse(SailingClass sailingClass) {
        return new SailingClassResponse(
                sailingClass.getId(),
                sailingClass.getWorldSailingId(),
                sailingClass.getName(),
                sailingClass.getClassCode(),
                sailingClass.getHullType(),
                sailingClass.getWorldSailingStatus(),
                sailingClass.getNumberOfCrew(),
                sailingClass.getNumberOfTrapeze(),
                sailingClass.getOptimalCrewWeight(),
                sailingClass.getHullLength(),
                sailingClass.getBeamLength(),
                sailingClass.getBoatWeight(),
                sailingClass.getHeadsailArea(),
                sailingClass.getMainsailArea(),
                sailingClass.getSpinnakerArea(),
                sailingClass.getClassDesigner(),
                sailingClass.getYearDesigned()
        );
    }
}
