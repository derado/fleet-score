package com.fleetscore.user.service;

import com.fleetscore.common.util.TokenGenerator;
import com.fleetscore.user.api.dto.RegistrationRequest;
import com.fleetscore.user.events.PasswordResetEmailRequested;
import com.fleetscore.user.events.VerificationEmailRequested;
import com.fleetscore.user.domain.PasswordResetToken;
import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.domain.VerificationToken;
import com.fleetscore.user.repository.InvitationRepository;
import com.fleetscore.user.repository.PasswordResetTokenRepository;
import com.fleetscore.user.repository.ProfileRepository;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.user.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserAccountRepository userRepository;
    @Mock ProfileRepository profileRepository;
    @Mock VerificationTokenRepository tokenRepository;
    @Mock InvitationRepository invitationRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationEventPublisher events;
    @Mock TokenGenerator tokenGenerator;

    @InjectMocks UserService userService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(userService, "resetTtlMinutes", 30);
    }

    @Test
    void registerUser_createsUser_savesToken_publishesEvent() {
        // given
        String email = "alice@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("ENC");
        when(tokenGenerator.generateHexToken(24)).thenReturn("VERIF_TOKEN");
        RegistrationRequest req = new RegistrationRequest(email, "Secret123!", "Alice", "Doe");

        // when
        String token = userService.registerUser(req);

        // then
        assertThat(token).isEqualTo("VERIF_TOKEN");
        verify(userRepository).save(argThat(u -> u instanceof UserAccount ua
                && ua.getEmail().equals(email)
                && ua.getPasswordHash().equals("ENC")
                && !ua.isEmailVerified()));
        verify(profileRepository).save(argThat(p -> p instanceof Profile pr
                && pr.getUser() != null
                && "Alice".equals(pr.getFirstName())
                && "Doe".equals(pr.getLastName())));
        verify(tokenRepository).save(argThat(v -> v instanceof VerificationToken vt && vt.getToken().equals("VERIF_TOKEN")));
        ArgumentCaptor<Object> ev1 = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(ev1.capture());
        Object published1 = ev1.getValue();
        assertTrue(published1 instanceof VerificationEmailRequested);
        VerificationEmailRequested ver1 = (VerificationEmailRequested) published1;
        assertThat(ver1.email()).isEqualTo(email);
        assertThat(ver1.token()).isEqualTo("VERIF_TOKEN");
    }

    @Test
    void requestPasswordReset_existingUser_createsToken_andPublishesEvent() {
        // given
        String email = "bob@example.com";
        UserAccount user = new UserAccount();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenGenerator.generateHexToken(24)).thenReturn("RST_TOKEN");

        // when
        userService.requestPasswordReset(email);

        // then
        verify(passwordResetTokenRepository).save(argThat(t -> t instanceof PasswordResetToken prt
                && prt.getToken().equals("RST_TOKEN")
                && prt.getUser() == user));
        ArgumentCaptor<Object> ev2 = ArgumentCaptor.forClass(Object.class);
        verify(events).publishEvent(ev2.capture());
        Object published2 = ev2.getValue();
        assertTrue(published2 instanceof PasswordResetEmailRequested);
        PasswordResetEmailRequested ev = (PasswordResetEmailRequested) published2;
        assertThat(ev.email()).isEqualTo(email);
        assertThat(ev.token()).isEqualTo("RST_TOKEN");
    }

    @Test
    void requestPasswordReset_nonExistingUser_noOp() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        userService.requestPasswordReset("nobody@example.com");
        verifyNoInteractions(passwordResetTokenRepository);
        verify(events, never()).publishEvent(any());
    }

    @Test
    void resetPassword_happyPath_updatesPassword_andMarksTokenUsed() {
        // given
        UserAccount user = new UserAccount();
        user.setEmail("c@example.com");
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("OK");
        prt.setUser(user);
        prt.setExpiresAt(Instant.now().plusSeconds(3600));
        when(passwordResetTokenRepository.findByToken("OK")).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("NewPass1!"))
                .thenReturn("ENC_NEW");

        // when
        userService.resetPassword("OK", "NewPass1!");

        // then
        verify(userRepository).save(argThat(u -> u instanceof UserAccount ua && ua.getPasswordHash().equals("ENC_NEW")));
        verify(passwordResetTokenRepository).save(argThat(t -> t instanceof PasswordResetToken p && p.getUsedAt() != null));
    }

    @Test
    void resetPassword_expired_throws() {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken("EX");
        prt.setExpiresAt(Instant.now().minusSeconds(1));
        when(passwordResetTokenRepository.findByToken("EX")).thenReturn(Optional.of(prt));

        assertThrows(IllegalStateException.class, () -> userService.resetPassword("EX", "whatever"));
    }
}
