package com.spotify.lld.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing helpers (demo-grade).
 * <p>
 * Why: plaintext passwords are an immediate security fail. Every password is
 * salted so rainbow tables and identical-password collisions are useless.
 * <p>
 * Logic: {@link #generateSalt} produces 16 cryptographically random bytes;
 * {@link #hash} runs SHA-256(salt || password) and Base64-encodes the digest.
 * Production should use Argon2id or BCrypt instead of raw SHA-256.
 */
public class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a unique Base64 salt for one user.
     * Logic: fill 16 random bytes via SecureRandom, encode for storage.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * One-way hash of password mixed with salt.
     * <p>
     * Logic: decode salt → MessageDigest SHA-256 update(salt) then digest(password)
     * → Base64 string for persistence. Never reversible to the original password.
     * <p>
     * In production, use BCrypt or Argon2 instead of SHA-256.
     */
    public static String hash(String password, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(Base64.getDecoder().decode(salt));
        byte[] hash = md.digest(password.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(hash);
    }
}
