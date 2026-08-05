package com.amazon.lld.account;

import com.amazon.lld.cart.ShoppingCart;
import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventListener;

/**
 * Registered buyer who can place orders and maintain a shopping cart.
 * <p>
 * Why: separates purchasable identity from guest browsing; owns a
 * {@link ShoppingCart} and may receive async order notifications.
 * <p>
 * Logic: wraps an {@link Account} with MEMBER or SELLER role and a dedicated
 * cart keyed by account id. Implements {@link OrderEventListener} so demos
 * can subscribe the member directly to the event bus.
 */
public class Member implements OrderEventListener {
    private final Account account;
    private final ShoppingCart cart;

    /**
     * Creates a member from an existing account (must be MEMBER or SELLER).
     *
     * @param account underlying account record
     */
    public Member(Account account) {
        this.account = account;
        this.cart = new ShoppingCart(account.getAccountId());
    }

    /** @return underlying account */
    public Account getAccount() { return account; }

    /** @return member's shopping cart */
    public ShoppingCart getCart() { return cart; }

    /** @return account id used as member id in orders */
    public String getMemberId() { return account.getAccountId(); }

    /** @return login username */
    public String getUsername() { return account.getUsername(); }

    /**
     * Receives order lifecycle notifications for this member.
     * Logic: prints a concise in-app style message (demo stub).
     */
    @Override
    public void onEvent(OrderEvent event) {
        if (event.getMemberId() != null && event.getMemberId().equals(getMemberId())) {
            System.out.println("  [member-app] " + account.getUsername() + ": "
                    + event.getType() + " — " + event.getPayload());
        }
    }
}
