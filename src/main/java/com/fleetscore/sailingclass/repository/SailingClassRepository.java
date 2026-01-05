package com.fleetscore.sailingclass.repository;

import com.fleetscore.sailingclass.domain.SailingClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SailingClassRepository extends JpaRepository<SailingClass, Long> {
}
