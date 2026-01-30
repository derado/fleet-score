package com.fleetscore.regatta.repository;

import com.fleetscore.regatta.domain.RaceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RaceResultRepository extends JpaRepository<RaceResult, Long> {

    @Query("SELECT rr FROM RaceResult rr " +
           "JOIN FETCH rr.registration reg " +
           "JOIN FETCH reg.sailingClass " +
           "JOIN FETCH reg.sailingNation " +
           "WHERE rr.race.id = :raceId " +
           "ORDER BY rr.points, rr.position")
    List<RaceResult> findByRaceId(@Param("raceId") Long raceId);

    @Query("SELECT COUNT(r) FROM Registration r " +
           "WHERE r.regatta.id = :regattaId " +
           "AND r.sailingClass.id = :sailingClassId")
    int countRegistrationsByRegattaAndClass(
            @Param("regattaId") Long regattaId,
            @Param("sailingClassId") Long sailingClassId);

    void deleteByRaceId(Long raceId);
}
