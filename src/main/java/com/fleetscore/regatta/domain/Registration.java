package com.fleetscore.regatta.domain;

import java.time.LocalDate;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.common.persistence.AuditableEntity;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingnation.domain.SailingNation;
import com.fleetscore.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Entity
@Table(name = "registrations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_registration_regatta_sail_number_class",
                columnNames = {"regatta_id", "sail_number", "sailing_class_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Registration extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regatta_id", nullable = false)
    private Regatta regatta;

    @Column(nullable = false, length = 100)
    private String sailorName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sailor_id")
    private Sailor sailor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender;

    @Column(nullable = false, length = 100)
    private String sailingClubName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sailing_club_id")
    private SailingClub sailingClub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sailing_class_id", nullable = false)
    private SailingClass sailingClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sailing_nation_id", nullable = false)
    private SailingNation sailingNation;

    @Column(nullable = false)
    private Integer sailNumber;
}
