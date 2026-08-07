package com.carrental.lld.account;

/**
 * Registered rental member with credentials and driver license.
 * <p>
 * Why: reservations, billing, and vehicle assignment are always tied to an
 * authenticated member identity.
 */
public class Member {
    private final String id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String name;
    private final String email;
    private final String licenseNumber;

    /**
     * @param id            unique member id
     * @param username      login name
     * @param passwordHash  salted password digest
     * @param salt          per-user salt
     * @param name          display name
     * @param email         contact email
     * @param licenseNumber driver's license for rental eligibility
     */
    public Member(String id, String username, String passwordHash, String salt,
                  String name, String email, String licenseNumber) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.name = name;
        this.email = email;
        this.licenseNumber = licenseNumber;
    }

    /** @return unique member id */
    public String getId() { return id; }

    /** @return login username */
    public String getUsername() { return username; }

    /** @return stored password hash */
    public String getPasswordHash() { return passwordHash; }

    /** @return password salt */
    public String getSalt() { return salt; }

    /** @return member display name */
    public String getName() { return name; }

    /** @return contact email */
    public String getEmail() { return email; }

    /** @return driver's license number */
    public String getLicenseNumber() { return licenseNumber; }
}
