package com.fleetscore.sailingclass.api.dto;

import com.fleetscore.sailingclass.domain.HullType;
import com.fleetscore.sailingclass.domain.WorldSailingStatus;

public record SailingClassResponse(
        Long id,
        String name,
        HullType hullType,
        WorldSailingStatus worldSailingStatus
) {
}
