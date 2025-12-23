package com.fleetscore.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "idx_invitation_token", columnList = "token", unique = true),
        @Index(name = "idx_invitation_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
