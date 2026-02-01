package com.fleetscore.club.service;

import com.fleetscore.club.repository.SailingClubRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClubAuthorizationService {

    private final SailingClubRepository sailingClubRepository;

    public boolean isAdmin(Long userId, Long clubId) {
        if (userId == null || clubId == null) {
            return false;
        }
        return sailingClubRepository.existsByIdAndAdmins_Id(clubId, userId);
    }

    public boolean isOwner(Long userId, Long clubId) {
        if (userId == null || clubId == null) {
            return false;
        }
        return sailingClubRepository.existsByIdAndOwner_Id(clubId, userId);
    }
}
