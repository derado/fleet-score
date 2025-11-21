package com.fleetscore.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "idx_invitation_token", columnList = "token", unique = true),
        @Index(name = "idx_invitation_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false, length = 320)
    private String email;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = true,
            foreignKey = @ForeignKey(name = "fk_invitation_org"))
    private Organisation organisation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "invitation_roles", joinColumns = @JoinColumn(name = "invitation_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 30, nullable = false)
    private Set<Role> roles = EnumSet.noneOf(Role.class);

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
