package com.fleetscore.club.internal;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
