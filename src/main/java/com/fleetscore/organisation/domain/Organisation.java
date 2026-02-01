package com.fleetscore.organisation.domain;

import com.fleetscore.common.persistence.AuditableEntity;
import com.fleetscore.user.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "organisations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_organisation_name", columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Organisation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String place;

    @Column(length = 20)
    private String postCode;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserAccount owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "organisation_admins",
            joinColumns = @JoinColumn(name = "organisation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserAccount> admins = new HashSet<>();
}
