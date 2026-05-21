package com.fleetscore.regatta.api.dto;

import com.fleetscore.regatta.domain.SeriesScoringType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateSeriesRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String description,
        @NotNull SeriesScoringType scoringType,
        @NotEmpty @Valid List<RegattaEntry> regattas,
        Integer throwoutAfter,
        Integer throwoutLimit
) {
    public record RegattaEntry(
            @NotNull Long regattaId,
            @Positive Double coefficient
    ) {}
}
