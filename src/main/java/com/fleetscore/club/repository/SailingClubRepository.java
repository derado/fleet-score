package com.fleetscore.club.repository;

import com.fleetscore.club.domain.SailingClub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SailingClubRepository extends JpaRepository<SailingClub, Long>, JpaSpecificationExecutor<SailingClub> {

    boolean existsByIdAndAdmins_Id(Long id, Long adminId);

    boolean existsByIdAndOwner_Id(Long id, Long ownerId);

    List<SailingClub> findByOwner_Id(Long ownerId);

    List<SailingClub> findByAdmins_Id(Long adminId);

    List<SailingClub> findByMembers_Id(Long memberId);
}
