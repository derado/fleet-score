package com.fleetscore.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                return Optional.of("system");
            }

            Object principal = auth.getPrincipal();
            if (principal instanceof Jwt jwt) {
                String email = jwt.getClaimAsString("email");
                if (email != null && !email.isBlank()) {
                    return Optional.of(email);
                }
            }

            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();
                if (username != null && !username.isBlank()) {
                    return Optional.of(username);
                }
            }

            String name = auth.getName();
            if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
                return Optional.of("system");
            }
            return Optional.of(name);
        };
    }
}
