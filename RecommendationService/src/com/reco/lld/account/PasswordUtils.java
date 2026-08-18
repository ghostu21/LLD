package com.reco.lld.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted password hashing (demo-grade).
 * <p>
 * Why: plaintext credentials are an immediate security fail. Per-user salt
 * defeats rainbow tables; constant-time compare reduces timing leaks.
 * <p>
 * Logic: {@link #generateSalt} fills 16 {@link SecureRandom} bytes;
 * {@link #hash} is SHA-256(salt || password). Production should use
 * Argon2id or BCrypt instead of a single SHA-256 round.
 */
public final class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {}

    /**
     * Generates a unique Base64 salt for one user.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * One-way hash of password mixed with salt. Never reversible.
     */
    public static String hash(String password, String salt) throws Exception {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("password and salt are required");
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest);
    }

    /**
     * Constant-time equality for hash strings (UTF-8 bytes).
     */
    public static boolean verify(String password, String salt, String expectedHash) throws Exception {
        if (expectedHash == null) return false;
        String actual = hash(password, salt);
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
