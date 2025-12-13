package com.fleetscore.user.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.events.PasswordResetEmailRequested;
import com.fleetscore.user.events.VerificationEmailRequested;
import com.fleetscore.user.domain.PasswordResetToken;
import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.domain.VerificationToken;
import com.fleetscore.user.repository.PasswordResetTokenRepository;
import com.fleetscore.user.repository.ProfileRepository;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.user.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@RecordApplicationEvents
@Transactional
class UserServiceTest {

    private static final Pattern HEX_UPPER = Pattern.compile("^[0-9A-F]+$");

    @Autowired UserAccountRepository userRepository;
    @Autowired ProfileRepository profileRepository;
    @Autowired VerificationTokenRepository tokenRepository;
    @Autowired PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserService userService;
    @Autowired ApplicationEvents applicationEvents;

    @Test
    void registerUser_createsUser_savesToken_publishesEvent() {
        // given
        String email = "alice@example.com";
        RegistrationRequest req = new RegistrationRequest(email, "Secret123!", "Alice", "Doe");

        // when
        String token = userService.registerUser(req);

        // then
        assertThat(token).isNotBlank();
        assertThat(token).hasSize(48);
        assertTrue(HEX_UPPER.matcher(token).matches());

        UserAccount saved = userRepository.findByEmail(email).orElseThrow();
        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPasswordHash()).isNotBlank();
        assertThat(saved.isEmailVerified()).isFalse();

        Profile profile = profileRepository.findByUser(saved).orElseThrow();
        assertThat(profile.getFirstName()).isEqualTo("Alice");
        assertThat(profile.getLastName()).isEqualTo("Doe");

        VerificationToken ver = tokenRepository.findByToken(token).orElseThrow();
        assertThat(ver.getUser().getId()).isEqualTo(saved.getId());
        assertThat(ver.getExpiresAt()).isAfter(Instant.now());

        var published = applicationEvents.stream(VerificationEmailRequested.class).toList();
        assertThat(published).hasSize(1);
        assertThat(published.getFirst().email()).isEqualTo(email);
        assertThat(published.getFirst().token()).isEqualTo(token);
    }

    @Test
    void requestPasswordReset_existingUser_createsToken_andPublishesEvent() {
        // given
        String email = "bob@example.com";
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user.setEmailVerified(true);
        userRepository.save(user);

        // when
        userService.requestPasswordReset(email);

        // then
        PasswordResetToken prt = passwordResetTokenRepository.findAll().stream().findFirst().orElseThrow();
        assertThat(prt.getUser().getEmail()).isEqualTo(email);
        assertThat(prt.getToken()).isNotBlank();
        assertThat(prt.getToken()).hasSize(48);
        assertTrue(HEX_UPPER.matcher(prt.getToken()).matches());
        assertThat(prt.getExpiresAt()).isAfter(Instant.now());

        var published = applicationEvents.stream(PasswordResetEmailRequested.class).toList();
        assertThat(published).hasSize(1);
        assertThat(published.getFirst().email()).isEqualTo(email);
        assertThat(published.getFirst().token()).isEqualTo(prt.getToken());
    }

    @Test
    void requestPasswordReset_nonExistingUser_noOp() {
        userService.requestPasswordReset("nobody@example.com");
        assertThat(passwordResetTokenRepository.findAll()).isEmpty();
        assertThat(applicationEvents.stream(PasswordResetEmailRequested.class).toList()).isEmpty();
    }

    @Test
    void resetPassword_happyPath_updatesPassword_andMarksTokenUsed() {
        // given
        UserAccount user = new UserAccount();
        user.setEmail("c@example.com");
        user.setPasswordHash(passwordEncoder.encode("OldPass1!"));
        user.setEmailVerified(true);
        userRepository.save(user);

        String oldHash = user.getPasswordHash();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("OK");
        prt.setUser(user);
        prt.setExpiresAt(Instant.now().plusSeconds(3600));
        passwordResetTokenRepository.save(prt);

        // when
        userService.resetPassword("OK", "NewPass1!");

        // then
        UserAccount updated = userRepository.findByEmail("c@example.com").orElseThrow();
        assertThat(updated.getPasswordHash()).isNotBlank();
        assertThat(updated.getPasswordHash()).isNotEqualTo(oldHash);
        assertTrue(passwordEncoder.matches("NewPass1!", updated.getPasswordHash()));

        PasswordResetToken saved = passwordResetTokenRepository.findByToken("OK").orElseThrow();
        assertThat(saved.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_expired_throws() {
        UserAccount user = new UserAccount();
        user.setEmail("expired@example.com");
        user.setPasswordHash(passwordEncoder.encode("OldPass1!"));
        user.setEmailVerified(true);
        userRepository.save(user);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("EX");
        prt.setUser(user);
        prt.setExpiresAt(Instant.now().minusSeconds(1));
        passwordResetTokenRepository.save(prt);

        assertThrows(IllegalStateException.class, () -> userService.resetPassword("EX", "whatever"));
    }
}
