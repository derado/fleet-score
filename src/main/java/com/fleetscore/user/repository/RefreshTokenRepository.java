package com.fleetscore.user.repository;

import com.fleetscore.user.domain.RefreshToken;
import com.fleetscore.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByUser(UserAccount user);
    long deleteByExpiresAtBefore(Instant time);
}
