package com.fleetscore.sailor.api.dto;

import java.time.LocalDate;

import com.fleetscore.common.domain.Gender;

public record SailorResponse(
        Long id,
        String name,
        String email,
        LocalDate dateOfBirth,
        Gender gender
) {}
