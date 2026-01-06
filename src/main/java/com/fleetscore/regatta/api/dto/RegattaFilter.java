package com.fleetscore.regatta.api.dto;

import java.time.LocalDate;

public record RegattaFilter(
        String name,
        LocalDate startDate,
        String venue,
        String sailingClass,
        String organiser,
        String organisation
) {
    public boolean isEmpty() {
        return name == null && startDate == null && venue == null
                && sailingClass == null && organiser == null && organisation == null;
    }
}
