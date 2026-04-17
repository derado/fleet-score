package com.fleetscore.regatta.internal;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.sailor.domain.Sailor;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class RegattaInternalApi {

    private final RegattaRepository regattaRepository;
    private final RegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public List<Regatta> findAll() {
        return regattaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Regatta findById(Long id) {
        return regattaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));
    }

    @Transactional(readOnly = true)
    public List<SailorSummary> findSailorsByUserId(Long userId) {
        return registrationRepository.findDistinctSailorsByUserId(userId).stream()
                .map(RegattaInternalApi::toSailorSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> findClubIdsByRegistrantUserId(Long userId) {
        return registrationRepository.findDistinctSailingClubIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<String> findExternalClubNamesByUserId(Long userId) {
        return registrationRepository.findDistinctExternalClubNamesByUserId(userId);
    }

    private static SailorSummary toSailorSummary(Sailor sailor) {
        return new SailorSummary(
                sailor.getId(),
                sailor.getName(),
                sailor.getEmail(),
                sailor.getDateOfBirth(),
                sailor.getGender()
        );
    }
}
