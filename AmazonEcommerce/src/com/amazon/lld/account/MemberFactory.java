package com.amazon.lld.account;

/**
 * Creates {@link Member} instances with salted password hashes.
 * <p>
 * Why: encapsulates member registration — callers never handle plaintext
 * persistence beyond the factory boundary.
 * <p>
 * Logic: generate salt → hash password → build Account → wrap in Member.
 */
public class MemberFactory implements AccountFactory<Member> {
    private final String username;
    private final String password;
    private final UserRole role;
    private final String name;
    private final String email;
    private final Address shippingAddress;

    /**
     * @param username        login name
     * @param password        plaintext password (hashed internally, never stored)
     * @param role            MEMBER or SELLER
     * @param name            display name
     * @param email           email address
     * @param shippingAddress default shipping address
     */
    public MemberFactory(String username, String password, UserRole role,
                         String name, String email, Address shippingAddress) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.shippingAddress = shippingAddress;
    }

    /**
     * Creates a member with hashed credentials and configured address.
     */
    @Override
    public Member create() throws Exception {
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hash(password, salt);
        Account account = new Account(username, hash, salt, role, name, email);
        account.setShippingAddress(shippingAddress);
        return new Member(account);
    }
}
