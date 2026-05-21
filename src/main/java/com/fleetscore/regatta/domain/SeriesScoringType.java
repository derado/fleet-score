package com.fleetscore.regatta.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeriesScoringType {

    REGATTA_PLACES(
            "Regatta Places",
            "Each regatta's overall finishing position becomes series points (1st = 1 pt, 2nd = 2 pts, …)."),

    SUM_RACE_POINTS(
            "Sum of Race Points",
            "Raw low-point race scores from every regatta are added together into one season total."),

    NORMALIZED(
            "Normalized Score",
            "Each regatta score is converted to a ratio of the worst possible score, making different fleet sizes comparable.");

    private final String displayName;
    private final String description;
}
