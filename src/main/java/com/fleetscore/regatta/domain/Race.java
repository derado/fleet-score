package com.fleetscore.regatta.domain;

import com.fleetscore.common.persistence.AuditableEntity;
import com.fleetscore.sailingclass.domain.SailingClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "races", uniqueConstraints = {
        @UniqueConstraint(name = "uk_race_regatta_number_class",
                columnNames = {"regatta_id", "race_number", "sailing_class_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Race extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regatta_id", nullable = false)
    private Regatta regatta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sailing_class_id", nullable = false)
    private SailingClass sailingClass;

    @Column(name = "race_number", nullable = false)
    private Integer raceNumber;

    @Column
    private LocalDate raceDate;
}
