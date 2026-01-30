package com.fleetscore.sailor.internal;

import java.time.LocalDate;
import java.util.Optional;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailor.repository.SailorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.NamedInterface;
import org.springframework.transaction.annotation.Transactional;

@NamedInterface("internal")
@RequiredArgsConstructor
public class SailorInternalApi {

    private final SailorRepository sailorRepository;

    @Transactional(readOnly = true)
    public Sailor findById(Long id) {
        return sailorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sailor not found"));
    }

    @Transactional(readOnly = true)
    public Optional<Sailor> findByEmail(String email) {
        return sailorRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Sailor> findByNameAndDateOfBirth(String name, LocalDate dateOfBirth) {
        return sailorRepository.findByNameAndDateOfBirth(name, dateOfBirth);
    }

    @Transactional
    public Sailor createSailor(String name, String email, LocalDate dateOfBirth, Gender gender) {
        Sailor sailor = new Sailor();
        sailor.setName(name);
        sailor.setEmail(email);
        sailor.setDateOfBirth(dateOfBirth);
        sailor.setGender(gender);
        return sailorRepository.save(sailor);
    }
}
