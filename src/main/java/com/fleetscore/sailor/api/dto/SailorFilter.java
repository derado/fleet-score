package com.fleetscore.sailor.api.dto;

import com.fleetscore.common.domain.Gender;

public record SailorFilter(
        String name,
        String email,
        Gender gender
) {}
