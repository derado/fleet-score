package com.fleetscore.regatta.domain;

import com.fleetscore.common.persistence.AuditableEntity;
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

@Entity
@Table(name = "series_regattas", uniqueConstraints = {
        @UniqueConstraint(name = "uk_series_regatta", columnNames = {"series_id", "regatta_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class SeriesRegatta extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regatta_id", nullable = false)
    private Regatta regatta;

    @Column(nullable = false)
    private Double coefficient = 1.0;
}
