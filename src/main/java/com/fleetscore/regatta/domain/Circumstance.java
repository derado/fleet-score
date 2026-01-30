package com.fleetscore.regatta.domain;

public enum Circumstance {
    DNC,  // Did Not Come to the starting area
    DNS,  // Did Not Start
    OCS,  // On the Course Side at start
    ZFP,  // Z Flag Penalty
    UFD,  // U Flag Disqualification
    BFD,  // Black Flag Disqualification
    SCP,  // Scoring Penalty
    NSC,  // Not Sailed the Course
    DNF,  // Did Not Finish
    RET,  // Retired
    DSQ,  // Disqualified
    DNE,  // Disqualification Not Excludable
    RDG,  // Redress Given
    DPI   // Discretionary Penalty Imposed
}
