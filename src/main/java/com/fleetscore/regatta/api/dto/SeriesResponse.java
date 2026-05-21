package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.SeriesScoringType;

import java.util.List;

public record SeriesResponse(
        Long id,
        String name,
        String description,
        SeriesScoringType scoringType,
        String ownerEmail,
        List<RegattaEntry> regattas,
        Integer throwoutAfter,
        Integer throwoutLimit
) {
    public record RegattaEntry(Long regattaId, Double coefficient) {}
}
