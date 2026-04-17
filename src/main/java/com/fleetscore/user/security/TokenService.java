package com.fleetscore.user.security;

import com.fleetscore.user.config.AuthProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final int accessTtlMinutes;

    public TokenService(JwtEncoder encoder, AuthProperties.Jwt jwt) {
        this.encoder = encoder;
        this.issuer = jwt.issuer();
        this.accessTtlMinutes = jwt.accessTtlMin();
    }

    public String generateAccessToken(String email, Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(email)
                .claim("uid", userId)
                .claim("email", email)
                .build();
        JwsHeader jws = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();
    }

    public Instant getExpiryFromNow() {
        return Instant.now().plus(accessTtlMinutes, ChronoUnit.MINUTES);
    }
}
