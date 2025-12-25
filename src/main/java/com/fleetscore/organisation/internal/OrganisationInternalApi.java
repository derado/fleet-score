package com.fleetscore.organisation.internal;

import com.fleetscore.organisation.domain.Organisation;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganisationInternalApi {

    private final OrganisationRepository organisationRepository;

    @Transactional(readOnly = true)
    public Organisation findById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Organisation> findByIdOptional(Long id) {
        return organisationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return organisationRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Long organisationId, Long userId) {
        return organisationRepository.existsByIdAndAdmins_Id(organisationId, userId);
    }
}
