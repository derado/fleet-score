package com.fleetscore.user.repository;

import com.fleetscore.user.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByName(String name);
    boolean existsByName(String name);
}
