package com.fleetscore.user.api.dto;

import com.fleetscore.common.domain.Gender;

import java.time.LocalDate;

public record MySailorResponse(
        Long id,
        String name,
        String email,
        LocalDate dateOfBirth,
        Gender gender
) {}