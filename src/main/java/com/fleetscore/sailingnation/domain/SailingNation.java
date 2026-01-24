package com.fleetscore.sailingnation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sailing_nations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_sailing_nation_code", columnNames = "code")
})
@Getter
@Setter
@NoArgsConstructor
public class SailingNation {

    @Id
    private Long id;

    @Column(nullable = false, length = 3)
    private String code;

    @Column(nullable = false, length = 30)
    private String country;
}
