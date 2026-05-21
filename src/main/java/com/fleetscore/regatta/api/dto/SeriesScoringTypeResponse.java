package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.SeriesScoringType;

public record SeriesScoringTypeResponse(
        SeriesScoringType value,
        String displayName,
        String description
) {}
