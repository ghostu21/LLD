package com.amazon.lld.command;

import com.amazon.lld.cart.Item;
import com.amazon.lld.cart.ShoppingCart;

/**
 * Command to add a line item to a cart with optimistic locking.
 * <p>
 * Why: wraps cart mutation so callers pass expectedVersion once.
 */
public class AddItemToCartCommand implements Command {
    private final ShoppingCart cart;
    private final Item item;
    private final int expectedVersion;

    /**
     * @param cart            target cart
     * @param item            line to add
     * @param expectedVersion client-known cart version
     */
    public AddItemToCartCommand(ShoppingCart cart, Item item, int expectedVersion) {
        this.cart = cart;
        this.item = item;
        this.expectedVersion = expectedVersion;
    }

    /** Adds item after version check. */
    @Override
    public void execute() {
        cart.addItem(item, expectedVersion);
    }
}
