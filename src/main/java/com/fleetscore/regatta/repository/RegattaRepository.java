package com.fleetscore.regatta.repository;

import com.fleetscore.regatta.domain.Regatta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegattaRepository extends JpaRepository<Regatta, Long> {
    boolean existsByIdAndAdmins_Id(Long regattaId, Long userId);
}
