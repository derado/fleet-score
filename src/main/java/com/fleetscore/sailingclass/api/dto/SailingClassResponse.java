package com.fleetscore.sailingclass.api.dto;

import com.fleetscore.sailingclass.domain.HullType;
import com.fleetscore.sailingclass.domain.WorldSailingStatus;

public record SailingClassResponse(
        Long id,
        String worldSailingId,
        String name,
        String classCode,
        HullType hullType,
        WorldSailingStatus worldSailingStatus,
        String numberOfCrew,
        String numberOfTrapeze,
        String optimalCrewWeight,
        String hullLength,
        String beamLength,
        String boatWeight,
        String headsailArea,
        String mainsailArea,
        String spinnakerArea,
        String classDesigner,
        String yearDesigned
) {
}
