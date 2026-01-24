package com.fleetscore.sailingclass.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sailing_classes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sailing_class_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class SailingClass {

    @Id
    private Long id;

    @Column(length = 36)
    private String worldSailingId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String classCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HullType hullType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorldSailingStatus worldSailingStatus;

    @Column(length = 50)
    private String numberOfCrew;

    @Column(length = 50)
    private String numberOfTrapeze;

    @Column(length = 20)
    private String optimalCrewWeight;

    @Column(length = 30)
    private String hullLength;

    @Column(length = 50)
    private String beamLength;

    @Column(length = 30)
    private String boatWeight;

    @Column(length = 30)
    private String headsailArea;

    @Column(length = 50)
    private String mainsailArea;

    @Column(length = 50)
    private String spinnakerArea;

    @Column(length = 50)
    private String classDesigner;

    @Column(length = 20)
    private String yearDesigned;
}
