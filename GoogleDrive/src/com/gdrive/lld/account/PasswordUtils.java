package com.gdrive.lld.account;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted SHA-256 password hashing (demo-grade).
 * <p>
 * Why: plaintext passwords fail an interview immediately. Production should
 * use Argon2id / BCrypt.
 */
public final class PasswordUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtils() {
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            return Base64.getEncoder().encodeToString(md.digest(password.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }
}
