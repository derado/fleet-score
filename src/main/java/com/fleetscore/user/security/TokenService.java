package com.fleetscore.user.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TokenService {

    private final JwtEncoder encoder;

    @Value("${app.auth.jwt.issuer:fleetscore}")
    private String issuer;

    @Value("${app.auth.jwt.access-ttl-min:15}")
    private int accessTtlMinutes;

    public TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
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
