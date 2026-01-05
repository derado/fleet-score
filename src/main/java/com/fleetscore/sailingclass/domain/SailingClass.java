package com.fleetscore.sailingclass.domain;

import com.fleetscore.common.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class SailingClass extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HullType hullType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorldSailingStatus worldSailingStatus;
}
