package com.fleetscore.sailingclass.repository;

import com.fleetscore.sailingclass.domain.SailingClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SailingClassRepository extends JpaRepository<SailingClass, Long> {

    @Query("SELECT sc FROM SailingClass sc WHERE " +
           "(:name IS NULL OR LOWER(sc.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(:classCode IS NULL OR LOWER(sc.classCode) LIKE LOWER(CONCAT('%', CAST(:classCode AS string), '%'))) AND " +
           "(:hullType IS NULL OR CAST(sc.hullType AS string) = :hullType) AND " +
           "(:worldSailingStatus IS NULL OR CAST(sc.worldSailingStatus AS string) = :worldSailingStatus)")
    List<SailingClass> findAllWithFilters(
            @Param("name") String name,
            @Param("classCode") String classCode,
            @Param("hullType") String hullType,
            @Param("worldSailingStatus") String worldSailingStatus);
}
