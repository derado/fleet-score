package com.fleetscore.regatta.service;

import com.fleetscore.regatta.repository.RegattaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RegattaAuthorizationService {

    private final RegattaRepository regattaRepository;

    public boolean isAdmin(Long userId, Long regattaId) {
        if (userId == null || regattaId == null) {
            return false;
        }
        return regattaRepository.existsByIdAndAdmins_Id(regattaId, userId);
    }

    public boolean isOwner(Long userId, Long regattaId) {
        if (userId == null || regattaId == null) {
            return false;
        }
        return regattaRepository.existsByIdAndOwner_Id(regattaId, userId);
    }
}
