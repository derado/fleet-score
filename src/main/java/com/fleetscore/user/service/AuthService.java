package com.fleetscore.user.service;

import com.fleetscore.security.TokenService;
import com.fleetscore.user.api.dto.LoginRequest;
import com.fleetscore.user.api.dto.TokenResponse;
import com.fleetscore.user.domain.RefreshToken;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.RefreshTokenRepository;
import com.fleetscore.user.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final TokenService tokenService;

    @Value("${app.auth.jwt.refresh-ttl-days:7}")
    private int refreshTtlDays;

    @Value("${app.auth.refresh.cookie-name:refresh_token}")
    private String cookieName;

    @Value("${app.auth.refresh.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.auth.refresh.cookie-samesite:Lax}")
    private String cookieSameSite;

    @Value("${app.auth.refresh.cookie-domain:}")
    private String cookieDomain;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public TokenResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        UserAccount user = users.findByEmail(request.email()).orElseThrow();
        String access = tokenService.generateAccessToken(
                user.getEmail(),
                user.getId()
        );
        Instant accessExp = tokenService.getExpiryFromNow();

        // issue refresh token
        String rt = generateToken();
        Instant rtExp = Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS);
        RefreshToken refresh = new RefreshToken();
        refresh.setToken(rt);
        refresh.setUser(user);
        refresh.setExpiresAt(rtExp);
        refreshTokens.save(refresh);

        setRefreshCookie(response, rt, rtExp, false);
        return new TokenResponse(access, accessExp);
    }

    @Transactional
    public TokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String rt = extractRefreshTokenFromCookie(request);
        if (rt == null) {
            throw new IllegalStateException("Missing refresh token");
        }
        RefreshToken token = refreshTokens.findByToken(rt)
                .orElseThrow(() -> new IllegalStateException("Invalid refresh token"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Refresh token expired or revoked");
        }
        UserAccount user = token.getUser();

        // rotate
        token.setRevoked(true);
        refreshTokens.save(token);
        String newRt = generateToken();
        Instant newRtExp = Instant.now().plus(refreshTtlDays, ChronoUnit.DAYS);
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(newRt);
        newToken.setUser(user);
        newToken.setExpiresAt(newRtExp);
        refreshTokens.save(newToken);

        // new access token
        String access = tokenService.generateAccessToken(
                user.getEmail(),
                user.getId()
        );
        Instant accessExp = tokenService.getExpiryFromNow();

        setRefreshCookie(response, newRt, newRtExp, false);
        return new TokenResponse(access, accessExp);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rt = extractRefreshTokenFromCookie(request);
        if (rt != null) {
            refreshTokens.findByToken(rt).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokens.save(t);
                refreshTokens.deleteByUser(t.getUser()); // simple family revocation
            });
        }
        // clear cookie
        setRefreshCookie(response, "", Instant.now().minusSeconds(60), true);
    }

    private void setRefreshCookie(HttpServletResponse response, String value, Instant exp, boolean delete) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(delete ? 0 : exp.getEpochSecond() - Instant.now().getEpochSecond());
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst().orElse(null);
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
