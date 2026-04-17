package com.fleetscore.club.internal;

import java.util.Set;

public record UserClubAssociation(
        Long id,
        String name,
        String place,
        Long sailingNationId,
        Set<Role> roles
) {
    public enum Role { OWNER, ADMIN, MEMBER }
}
