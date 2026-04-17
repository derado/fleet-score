package com.fleetscore.regatta.internal;

import com.fleetscore.common.domain.Gender;
import java.time.LocalDate;

public record SailorSummary(
        Long id,
        String name,
        String email,
        LocalDate dateOfBirth,
        Gender gender
) {}
