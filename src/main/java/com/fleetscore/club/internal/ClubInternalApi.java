package com.fleetscore.club.internal;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ClubInternalApi {

    private final SailingClubRepository sailingClubRepository;

    @Transactional(readOnly = true)
    public List<SailingClub> findAll() {
        return sailingClubRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SailingClub findById(Long id) {
        return sailingClubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
    }

    @Transactional(readOnly = true)
    public List<UserClubAssociation> findUserClubAssociations(Long userId) {
        Map<Long, ClubWithRoles> byId = new LinkedHashMap<>();
        mergeClubs(byId, sailingClubRepository.findByOwner_Id(userId), UserClubAssociation.Role.OWNER);
        mergeClubs(byId, sailingClubRepository.findByAdmins_Id(userId), UserClubAssociation.Role.ADMIN);
        mergeClubs(byId, sailingClubRepository.findByMembers_Id(userId), UserClubAssociation.Role.MEMBER);

        return byId.values().stream()
                .map(ClubInternalApi::toAssociation)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClubSummary> findSummariesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return sailingClubRepository.findAllById(ids).stream()
                .map(ClubInternalApi::toSummary)
                .toList();
    }

    private static ClubSummary toSummary(SailingClub club) {
        return new ClubSummary(
                club.getId(),
                club.getName(),
                club.getPlace(),
                club.getSailingNation() != null ? club.getSailingNation().getId() : null
        );
    }

    private static void mergeClubs(
            Map<Long, ClubWithRoles> target,
            Collection<SailingClub> clubs,
            UserClubAssociation.Role role
    ) {
        clubs.forEach(club -> target
                .computeIfAbsent(club.getId(), id -> new ClubWithRoles(club, EnumSet.noneOf(UserClubAssociation.Role.class)))
                .roles().add(role));
    }

    private static UserClubAssociation toAssociation(ClubWithRoles entry) {
        SailingClub club = entry.club();
        return new UserClubAssociation(
                club.getId(),
                club.getName(),
                club.getPlace(),
                club.getSailingNation() != null ? club.getSailingNation().getId() : null,
                EnumSet.copyOf(entry.roles())
        );
    }

    private record ClubWithRoles(SailingClub club, EnumSet<UserClubAssociation.Role> roles) {}
}
