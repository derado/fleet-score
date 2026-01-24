package com.fleetscore.user.service;

import com.fleetscore.user.api.dto.MeResponse;
import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.domain.*;
import com.fleetscore.user.repository.*;
import com.fleetscore.common.exception.EmailAlreadyInUseException;
import com.fleetscore.common.exception.InvalidTokenException;
import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.common.exception.TokenAlreadyUsedException;
import com.fleetscore.common.exception.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import com.fleetscore.user.events.InvitationEmailRequested;
import com.fleetscore.user.events.PasswordResetEmailRequested;
import com.fleetscore.user.events.VerificationEmailRequested;
import com.fleetscore.common.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userRepository;
    private final ProfileRepository profileRepository;
    private final VerificationTokenRepository tokenRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final TokenGenerator tokenGenerator;

    

    @Value("${app.auth.reset-ttl-min:30}")
    private int resetTtlMinutes;

    @Transactional
    public String registerUser(RegistrationRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyInUseException(req.email());
        }
        UserAccount user = new UserAccount();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setEmailVerified(false);
        userRepository.save(user);

        String token = tokenGenerator.generateHexToken(24);
        VerificationToken ver = new VerificationToken();
        ver.setToken(token);
        ver.setUser(user);
        ver.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        tokenRepository.save(ver);

        events.publishEvent(new VerificationEmailRequested(user.getEmail(), token));
        return token;
    }

    @Transactional
    public Profile upsertProfile(String email, String firstName, String lastName) {
        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        Profile profile = profileRepository.findByUser(user).orElseGet(() -> {
            Profile p = new Profile();
            p.setUser(user);
            return p;
        });

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        return profileRepository.save(profile);
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken ver = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("verification"));
        if (ver.isUsed()) {
            throw new TokenAlreadyUsedException("Verification");
        }
        if (ver.isExpired()) {
            throw new TokenExpiredException("Verification");
        }
        UserAccount user = ver.getUser();
        user.setEmailVerified(true);
        ver.setUsedAt(Instant.now());
        userRepository.save(user);
        tokenRepository.save(ver);
    }

    @Transactional
    public String createInvitation(String email, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }
        String token = tokenGenerator.generateHexToken(24);
        Invitation inv = new Invitation();
        inv.setToken(token);
        inv.setEmail(email);
        inv.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitationRepository.save(inv);

        events.publishEvent(new InvitationEmailRequested(email, token));
        return token;
    }

    @Transactional
    public void acceptInvitation(String token, String password) {
        Invitation inv = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("invitation"));
        if (inv.isUsed()) {
            throw new TokenAlreadyUsedException("Invitation");
        }
        if (inv.isExpired()) {
            throw new TokenExpiredException("Invitation");
        }
        if (userRepository.existsByEmail(inv.getEmail())) {
            throw new EmailAlreadyInUseException(inv.getEmail());
        }
        UserAccount user = new UserAccount();
        user.setEmail(inv.getEmail());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmailVerified(true);
        userRepository.save(user);
        inv.setUsedAt(Instant.now());
        invitationRepository.save(inv);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = tokenGenerator.generateHexToken(24);
            PasswordResetToken prt = new PasswordResetToken();
            prt.setToken(token);
            prt.setUser(user);
            prt.setExpiresAt(Instant.now().plus(resetTtlMinutes, ChronoUnit.MINUTES));
            passwordResetTokenRepository.save(prt);

            events.publishEvent(new PasswordResetEmailRequested(user.getEmail(), token));
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("password reset"));
        if (prt.isUsed()) {
            throw new TokenAlreadyUsedException("Password reset");
        }
        if (prt.isExpired()) {
            throw new TokenExpiredException("Password reset");
        }
        UserAccount user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isEmailVerified()) {
                return; // no-op if already verified
            }
            String token = tokenGenerator.generateHexToken(24);
            VerificationToken ver = new VerificationToken();
            ver.setToken(token);
            ver.setUser(user);
            ver.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
            tokenRepository.save(ver);
            events.publishEvent(new VerificationEmailRequested(user.getEmail(), token));
        });
    }

    @Transactional(readOnly = true)
    public MeResponse getCurrentUser(String email) {
        if (email == null) {
            return new MeResponse(false, null, null, null, null, false);
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    Profile profile = profileRepository.findByUser(user).orElse(null);
                    boolean profileCreated = profile != null;
                    String firstName = profileCreated ? profile.getFirstName() : null;
                    String lastName = profileCreated ? profile.getLastName() : null;
                    return new MeResponse(true, email, firstName, lastName, user.isEmailVerified(), profileCreated);
                })
                .orElse(new MeResponse(false, null, null, null, null, false));
    }
}
