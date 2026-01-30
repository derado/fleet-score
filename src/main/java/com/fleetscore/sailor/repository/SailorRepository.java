package com.fleetscore.sailor.repository;

import java.time.LocalDate;
import java.util.Optional;

import com.fleetscore.sailor.domain.Sailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SailorRepository extends JpaRepository<Sailor, Long>, JpaSpecificationExecutor<Sailor> {

    Optional<Sailor> findByEmail(String email);

    Optional<Sailor> findByNameAndDateOfBirth(String name, LocalDate dateOfBirth);

    boolean existsByEmail(String email);
}
