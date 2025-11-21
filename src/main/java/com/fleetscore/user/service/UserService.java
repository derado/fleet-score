package com.fleetscore.user.service;

import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.domain.*;
import com.fleetscore.user.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.fleetscore.common.events.InvitationEmailRequested;
import com.fleetscore.common.events.VerificationEmailRequested;
import com.fleetscore.common.events.PasswordResetEmailRequested;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final OrganisationRepository organisationRepository;
    private final UserAccountRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final InvitationRepository invitationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.auth.reset-ttl-min:30}")
    private int resetTtlMinutes;

    @Transactional
    public String registerAndCreateOrganisation(RegistrationRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalStateException("Email already in use");
        }
        if (organisationRepository.existsByName(req.organisationName())) {
            throw new IllegalStateException("Organisation already exists");
        }
        Organisation organisation = new Organisation();
        organisation.setName(req.organisationName());
        organisationRepository.save(organisation);

        UserAccount user = new UserAccount();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setOrganisation(organisation);
        user.setEmailVerified(false);
        user.addRole(Role.ADMIN);
        userRepository.save(user);

        String token = generateToken();
        VerificationToken ver = new VerificationToken();
        ver.setToken(token);
        ver.setUser(user);
        ver.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        tokenRepository.save(ver);

        events.publishEvent(new VerificationEmailRequested(user.getEmail(), token));
        return token;
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken ver = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid verification token"));
        if (ver.isUsed()) {
            throw new IllegalStateException("Token already used");
        }
        if (ver.isExpired()) {
            throw new IllegalStateException("Token expired");
        }
        UserAccount user = ver.getUser();
        user.setEmailVerified(true);
        ver.setUsedAt(Instant.now());
        userRepository.save(user);
        tokenRepository.save(ver);
    }

    @Transactional
    public String createInvitation(String email, Set<Role> roles, Long adminUserId) {
        UserAccount admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found"));
        if (!admin.getRoles().contains(Role.ADMIN)) {
            throw new IllegalStateException("Only ADMIN can invite users");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with email already exists");
        }
        String token = generateToken();
        Invitation inv = new Invitation();
        inv.setToken(token);
        inv.setEmail(email);
        inv.setOrganisation(admin.getOrganisation());
        inv.setRoles(roles);
        inv.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        invitationRepository.save(inv);

        events.publishEvent(new InvitationEmailRequested(email, token));
        return token;
    }

    @Transactional
    public void acceptInvitation(String token, String password) {
        Invitation inv = invitationRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid invitation token"));
        if (inv.isUsed()) {
            throw new IllegalStateException("Invitation already used");
        }
        if (inv.isExpired()) {
            throw new IllegalStateException("Invitation expired");
        }
        if (userRepository.existsByEmail(inv.getEmail())) {
            throw new IllegalStateException("User with email already exists");
        }
        UserAccount user = new UserAccount();
        user.setEmail(inv.getEmail());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setOrganisation(inv.getOrganisation());
        user.setEmailVerified(true);
        user.setRoles(inv.getRoles());
        userRepository.save(user);
        inv.setUsedAt(Instant.now());
        invitationRepository.save(inv);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = generateToken();
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
                .orElseThrow(() -> new EntityNotFoundException("Invalid password reset token"));
        if (prt.isUsed()) {
            throw new IllegalStateException("Token already used");
        }
        if (prt.isExpired()) {
            throw new IllegalStateException("Token expired");
        }
        UserAccount user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
