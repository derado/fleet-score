package com.fleetscore.user.api.dto;

import java.util.Set;

public record MyClubResponse(
        Long id,
        String name,
        String place,
        Long sailingNationId,
        Set<Relationship> relationships
) {
    public enum Relationship { OWNER, ADMIN, MEMBER }
}
