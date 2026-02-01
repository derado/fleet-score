package com.fleetscore.regatta.repository;

import com.fleetscore.regatta.domain.Regatta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegattaRepository extends JpaRepository<Regatta, Long>, JpaSpecificationExecutor<Regatta> {
    boolean existsByIdAndAdmins_Id(Long regattaId, Long userId);

    boolean existsByIdAndOwner_Id(Long regattaId, Long userId);
}
