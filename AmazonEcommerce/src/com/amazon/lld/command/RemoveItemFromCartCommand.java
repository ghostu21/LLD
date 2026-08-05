package com.amazon.lld.command;

import com.amazon.lld.cart.ShoppingCart;

/**
 * Command to remove a product line from the cart with optimistic locking.
 */
public class RemoveItemFromCartCommand implements Command {
    private final ShoppingCart cart;
    private final String productId;
    private final int expectedVersion;

    /**
     * @param cart            target cart
     * @param productId       product to remove
     * @param expectedVersion client-known cart version
     */
    public RemoveItemFromCartCommand(ShoppingCart cart, String productId, int expectedVersion) {
        this.cart = cart;
        this.productId = productId;
        this.expectedVersion = expectedVersion;
    }

    /** Removes item after version check. */
    @Override
    public void execute() {
        cart.removeItem(productId, expectedVersion);
    }
}
