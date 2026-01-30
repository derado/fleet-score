package com.fleetscore.sailor.service;

import java.util.List;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.sailor.api.dto.SailorFilter;
import com.fleetscore.sailor.api.dto.SailorRequest;
import com.fleetscore.sailor.api.dto.SailorResponse;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailor.repository.SailorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SailorService {

    private final SailorRepository sailorRepository;

    @Transactional(readOnly = true)
    public List<SailorResponse> findAllSailors(SailorFilter filter) {
        Specification<Sailor> spec = Specification.where((Specification<Sailor>) null);

        if (filter.name() != null && !filter.name().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
        }
        if (filter.email() != null && !filter.email().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + filter.email().toLowerCase() + "%"));
        }
        if (filter.gender() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("gender"), filter.gender()));
        }

        return sailorRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SailorResponse findSailorById(Long id) {
        Sailor sailor = sailorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailor not found"));
        return toResponse(sailor);
    }

    @Transactional
    public SailorResponse createSailor(SailorRequest request) {
        if (sailorRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Sailor with this email already exists");
        }

        Sailor sailor = new Sailor();
        sailor.setName(request.name());
        sailor.setEmail(request.email());
        sailor.setDateOfBirth(request.dateOfBirth());
        sailor.setGender(request.gender());

        Sailor saved = sailorRepository.save(sailor);
        return toResponse(saved);
    }

    @Transactional
    public SailorResponse updateSailor(Long id, SailorRequest request) {
        Sailor sailor = sailorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailor not found"));

        if (!sailor.getEmail().equals(request.email()) && sailorRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Sailor with this email already exists");
        }

        sailor.setName(request.name());
        sailor.setEmail(request.email());
        sailor.setDateOfBirth(request.dateOfBirth());
        sailor.setGender(request.gender());

        return toResponse(sailor);
    }

    private SailorResponse toResponse(Sailor sailor) {
        return new SailorResponse(
                sailor.getId(),
                sailor.getName(),
                sailor.getEmail(),
                sailor.getDateOfBirth(),
                sailor.getGender()
        );
    }
}
