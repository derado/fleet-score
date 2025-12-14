package com.fleetscore.organisation.repository;

import com.fleetscore.organisation.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    boolean existsByName(String name);
}
