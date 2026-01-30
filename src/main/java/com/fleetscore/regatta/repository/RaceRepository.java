package com.fleetscore.regatta.repository;

import com.fleetscore.regatta.domain.Race;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RaceRepository extends JpaRepository<Race, Long> {

    @Query("SELECT r FROM Race r " +
           "JOIN FETCH r.sailingClass " +
           "WHERE r.regatta.id = :regattaId " +
           "ORDER BY r.sailingClass.name, r.raceNumber")
    List<Race> findByRegattaId(@Param("regattaId") Long regattaId);

    @Query("SELECT r FROM Race r " +
           "JOIN FETCH r.sailingClass " +
           "WHERE r.regatta.id = :regattaId " +
           "AND r.sailingClass.id = :sailingClassId " +
           "ORDER BY r.raceNumber")
    List<Race> findByRegattaIdAndSailingClassId(
            @Param("regattaId") Long regattaId,
            @Param("sailingClassId") Long sailingClassId);

    @Query("SELECT r FROM Race r " +
           "JOIN FETCH r.sailingClass " +
           "WHERE r.id = :raceId")
    Optional<Race> findByIdWithSailingClass(@Param("raceId") Long raceId);

    boolean existsByRegattaIdAndRaceNumberAndSailingClassId(Long regattaId, Integer raceNumber, Long sailingClassId);
}
