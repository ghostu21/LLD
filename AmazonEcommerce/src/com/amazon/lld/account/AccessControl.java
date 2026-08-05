package com.amazon.lld.account;

/**
 * Centralized authorization checks for marketplace operations.
 * <p>
 * Why: keeps access rules in one place instead of scattering role checks
 * across checkout, catalog, and seller flows.
 * <p>
 * Logic: guests may browse; only active members/sellers may purchase;
 * only sellers/admins may list products.
 */
public final class AccessControl {

    private AccessControl() {}

    /**
     * Any visitor (guest or member) may search and view products.
     *
     * @param actor guest or member
     * @return always true for non-null actor
     */
    public static boolean canBrowse(Object actor) {
        return actor != null;
    }

    /**
     * Only active members (or sellers acting as buyers) may checkout.
     *
     * @param member registered member
     * @return true if member account is ACTIVE
     */
    public static boolean canPurchase(Member member) {
        if (member == null) return false;
        Account account = member.getAccount();
        return account.getStatus() == AccountStatus.ACTIVE
                && (account.getRole() == UserRole.MEMBER || account.getRole() == UserRole.SELLER);
    }

    /**
     * Only sellers and admins may add products to the catalog.
     *
     * @param account seller or admin account
     * @return true if role permits selling
     */
    public static boolean canSell(Account account) {
        if (account == null) return false;
        return account.getStatus() == AccountStatus.ACTIVE
                && (account.getRole() == UserRole.SELLER || account.getRole() == UserRole.ADMIN);
    }

    /**
     * Enforces purchase permission or throws.
     *
     * @param member candidate buyer
     * @throws AccessDeniedException if not permitted
     */
    public static void requirePurchase(Member member) {
        if (!canPurchase(member)) {
            throw new AccessDeniedException("Only active members may checkout");
        }
    }

    /**
     * Enforces sell permission or throws.
     *
     * @param account seller account
     * @throws AccessDeniedException if not permitted
     */
    public static void requireSell(Account account) {
        if (!canSell(account)) {
            throw new AccessDeniedException("Only sellers may manage products");
        }
    }
}
