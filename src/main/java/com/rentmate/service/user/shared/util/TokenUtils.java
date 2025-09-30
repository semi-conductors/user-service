package com.rentmate.service.user.shared.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


public class TokenUtils {
    public static String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 256-bit
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hashToken(String rawToken) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
    }
}
