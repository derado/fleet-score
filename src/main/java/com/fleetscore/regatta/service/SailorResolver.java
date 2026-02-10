package com.fleetscore.regatta.service;

import java.time.LocalDate;

import com.fleetscore.common.domain.Gender;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailor.internal.SailorInternalApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SailorResolver {

    private final SailorInternalApi sailorApi;

    public Sailor findOrCreate(Long sailorId, String email, String name, LocalDate dateOfBirth, Gender gender) {
        if (sailorId != null) {
            return sailorApi.findById(sailorId);
        }

        return sailorApi.findByNameAndDateOfBirth(name, dateOfBirth)
                .orElseGet(() -> sailorApi.createSailor(name, email, dateOfBirth, gender));
    }
}
