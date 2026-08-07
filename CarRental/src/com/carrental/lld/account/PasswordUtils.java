package com.carrental.lld.account;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing helpers (demo-grade).
 * <p>
 * Why: member accounts must never store plaintext passwords. Salting prevents
 * rainbow-table and identical-password collisions across users.
 * <p>
 * Logic: {@link #generateSalt} produces 16 cryptographically random bytes;
 * {@link #hash} runs SHA-256(salt || password) and Base64-encodes the digest.
 * Production should use Argon2id or BCrypt instead of raw SHA-256.
 */
public final class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {}

    /**
     * Generates a unique Base64 salt for one member.
     *
     * @return encoded salt bytes
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * One-way hash of password mixed with salt.
     *
     * @param password plaintext password
     * @param salt     stored salt for the member
     * @return Base64-encoded digest
     * @throws Exception if SHA-256 is unavailable
     */
    public static String hash(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }
}
