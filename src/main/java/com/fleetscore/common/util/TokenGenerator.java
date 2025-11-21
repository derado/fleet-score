package com.fleetscore.common.util;

import org.springframework.stereotype.Component;
import org.springframework.modulith.NamedInterface;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@NamedInterface
public class TokenGenerator {
    private final SecureRandom random = new SecureRandom();

    public String generateHexToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        random.nextBytes(bytes);
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
