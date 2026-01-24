package com.fleetscore.sailingnation.repository;

import com.fleetscore.sailingnation.domain.SailingNation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SailingNationRepository extends JpaRepository<SailingNation, Long> {

    @Query("SELECT sn FROM SailingNation sn WHERE " +
           "(:code IS NULL OR LOWER(sn.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:country IS NULL OR LOWER(sn.country) LIKE LOWER(CONCAT('%', :country, '%')))")
    List<SailingNation> findAllWithFilters(@Param("code") String code, @Param("country") String country);
}
