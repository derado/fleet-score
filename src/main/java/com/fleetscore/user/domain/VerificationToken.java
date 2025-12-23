package com.fleetscore.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "verification_tokens", indexes = {
        @Index(name = "idx_verification_token_token", columnList = "token", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_verification_user"))
    private UserAccount user;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    public boolean isUsed() { return usedAt != null; }
    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
}
