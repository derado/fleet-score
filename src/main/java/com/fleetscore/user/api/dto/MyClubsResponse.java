package com.fleetscore.user.api.dto;

import java.util.List;

public record MyClubsResponse(
        List<MyClubResponse> memberships,
        List<RegisteredClubResponse> registeredClubs,
        List<String> externalClubs
) {}
