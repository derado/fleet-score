package com.fleetscore.security;

import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.internal.UserInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collections;

@RequiredArgsConstructor
public class UserAccountJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserInternalApi userInternalApi;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Missing email claim");
        }
        UserAccount user = userInternalApi.findByEmailOptional(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return new UsernamePasswordAuthenticationToken(user, jwt, Collections.emptyList());
    }
}
