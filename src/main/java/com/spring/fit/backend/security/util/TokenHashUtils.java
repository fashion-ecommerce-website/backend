package com.spring.fit.backend.security.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class TokenHashUtils {

    private TokenHashUtils() {}

    /**
     * Hashes a raw token string using SHA-256 and returns a hex string.
     */
    public static String hashToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return token;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
