package com.fleetscore.user.config;

import com.fleetscore.common.util.TokenGenerator;
import com.fleetscore.user.internal.UserInternalApi;
import com.fleetscore.user.repository.InvitationRepository;
import com.fleetscore.user.repository.PasswordResetTokenRepository;
import com.fleetscore.user.repository.ProfileRepository;
import com.fleetscore.user.repository.RefreshTokenRepository;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.user.repository.VerificationTokenRepository;
import com.fleetscore.user.security.CustomUserDetailsService;
import com.fleetscore.user.security.TokenService;
import com.fleetscore.user.service.AuthService;
import com.fleetscore.user.service.UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class UserConfig {

    @Bean
    UserInternalApi userInternalApi(UserAccountRepository userAccountRepository) {
        return new UserInternalApi(userAccountRepository);
    }

    @Bean
    CustomUserDetailsService customUserDetailsService(UserAccountRepository users) {
        return new CustomUserDetailsService(users);
    }

    @Bean
    TokenService tokenService(JwtEncoder encoder, AuthProperties auth) {
        return new TokenService(encoder, auth.jwt());
    }

    @Bean
    AuthService authService(
            AuthenticationManager authenticationManager,
            UserAccountRepository users,
            RefreshTokenRepository refreshTokens,
            TokenService tokenService,
            TokenGenerator tokenGenerator,
            AuthProperties auth) {
        return new AuthService(authenticationManager, users, refreshTokens, tokenService, tokenGenerator,
                auth.jwt(), auth.refresh());
    }

    @Bean
    UserService userService(
            UserAccountRepository userRepository,
            ProfileRepository profileRepository,
            VerificationTokenRepository tokenRepository,
            InvitationRepository invitationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events,
            TokenGenerator tokenGenerator,
            AuthProperties auth) {
        return new UserService(
                userRepository, profileRepository, tokenRepository, invitationRepository,
                passwordResetTokenRepository, passwordEncoder, events, tokenGenerator, auth);
    }
}
