package com.amazon.lld.account;

import java.util.UUID;

/**
 * Base registered-user record with salted password hash (never plaintext).
 * <p>
 * Why: central identity for members and sellers; password verification uses
 * stored hash + salt via {@link PasswordUtils}.
 * <p>
 * Logic: constructed with role and ACTIVE status; {@link #verifyPassword}
 * re-hashes the candidate and compares to stored hash.
 */
public class Account {
    private final String accountId;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final UserRole role;
    private String name;
    private String email;
    private String phone;
    private Address shippingAddress;
    private AccountStatus status;

    /**
     * Creates a new account with hashed credentials.
     *
     * @param username     unique login name
     * @param passwordHash salted hash from {@link PasswordUtils#hash}
     * @param salt         per-user salt
     * @param role         authorization role
     * @param name         display name
     * @param email        contact email
     */
    public Account(String username, String passwordHash, String salt, UserRole role,
                   String name, String email) {
        this.accountId = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.name = name;
        this.email = email;
        this.status = AccountStatus.ACTIVE;
    }

    /** @return stable account identifier */
    public String getAccountId() { return accountId; }

    /** @return login username */
    public String getUsername() { return username; }

    /** @return stored password hash (never plaintext) */
    public String getPasswordHash() { return passwordHash; }

    /** @return per-user salt */
    public String getSalt() { return salt; }

    /** @return authorization role */
    public UserRole getRole() { return role; }

    /** @return display name */
    public String getName() { return name; }

    /** @return contact email */
    public String getEmail() { return email; }

    /** @return phone number, may be null */
    public String getPhone() { return phone; }

    /** @return default shipping address, may be null */
    public Address getShippingAddress() { return shippingAddress; }

    /** @return account lifecycle status */
    public AccountStatus getStatus() { return status; }

    /** Updates display name. */
    public void setName(String name) { this.name = name; }

    /** Updates contact email. */
    public void setEmail(String email) { this.email = email; }

    /** Updates phone number. */
    public void setPhone(String phone) { this.phone = phone; }

    /** Updates default shipping address. */
    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    /** Updates lifecycle status (e.g. block account). */
    public void setStatus(AccountStatus status) { this.status = status; }

    /**
     * Verifies a candidate password against the stored hash.
     * <p>
     * Logic: hash(candidate, salt) and compare to passwordHash.
     *
     * @param candidate plaintext password from login form
     * @return true if password matches
     */
    public boolean verifyPassword(String candidate) throws Exception {
        return PasswordUtils.hash(candidate, salt).equals(passwordHash);
    }
}
