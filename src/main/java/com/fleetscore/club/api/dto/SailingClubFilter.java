package com.fleetscore.club.api.dto;

public record SailingClubFilter(
        String name,
        Long sailingNationId
) {
    public boolean isEmpty() {
        return name == null && sailingNationId == null;
    }
}