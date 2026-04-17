package com.fleetscore.regatta.repository;

import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.sailor.domain.Sailor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    @Query("SELECT DISTINCT r.sailor FROM Registration r WHERE r.user.id = :userId AND r.sailor IS NOT NULL")
    List<Sailor> findDistinctSailorsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.sailingClub.id FROM Registration r WHERE r.user.id = :userId AND r.sailingClub IS NOT NULL")
    Set<Long> findDistinctSailingClubIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT r.sailingClubName FROM Registration r " +
           "WHERE r.user.id = :userId AND r.sailingClub IS NULL")
    List<String> findDistinctExternalClubNamesByUserId(@Param("userId") Long userId);

    boolean existsByRegattaId(Long regattaId);

    boolean existsByRegattaIdAndSailNumberAndSailingClassId(Long regattaId, Integer sailNumber, Long sailingClassId);

    boolean existsByRegattaIdAndSailNumberAndSailingClassIdAndIdNot(Long regattaId, Integer sailNumber, Long sailingClassId, Long id);

    @Query("SELECT r FROM Registration r " +
           "JOIN FETCH r.sailingClass " +
           "JOIN FETCH r.sailingNation " +
           "LEFT JOIN FETCH r.sailingClub " +
           "LEFT JOIN FETCH r.user " +
           "WHERE r.regatta.id = :regattaId " +
           "AND (:sailingClassId IS NULL OR r.sailingClass.id = :sailingClassId) " +
           "AND (:sailingNationId IS NULL OR r.sailingNation.id = :sailingNationId) " +
           "AND (:sailorName IS NULL OR LOWER(r.sailorName) LIKE LOWER(CONCAT('%', CAST(:sailorName AS string), '%'))) " +
           "AND (:sailingClubName IS NULL OR LOWER(r.sailingClubName) LIKE LOWER(CONCAT('%', CAST(:sailingClubName AS string), '%'))) " +
           "AND (:sailNumber IS NULL OR r.sailNumber = :sailNumber) " +
           "AND (:gender IS NULL OR r.gender = :gender) " +
           "ORDER BY r.sailingClass.name, r.createdAt")
    List<Registration> findByRegattaIdWithFilters(
            @Param("regattaId") Long regattaId,
            @Param("sailingClassId") Long sailingClassId,
            @Param("sailingNationId") Long sailingNationId,
            @Param("sailorName") String sailorName,
            @Param("sailingClubName") String sailingClubName,
            @Param("sailNumber") Integer sailNumber,
            @Param("gender") Gender gender);
}
