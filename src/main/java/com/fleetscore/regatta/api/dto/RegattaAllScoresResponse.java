package com.fleetscore.regatta.api.dto;

import java.util.List;

public record RegattaAllScoresResponse(
        Long regattaId,
        List<RegattaScoreResponse> classes
) {}
