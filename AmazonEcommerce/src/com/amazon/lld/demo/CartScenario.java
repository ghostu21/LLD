package com.amazon.lld.demo;

import com.amazon.lld.cart.CartVersionException;
import com.amazon.lld.cart.Item;
import com.amazon.lld.command.AddItemToCartCommand;

/**
 * Demo: optimistic locking on cart mutations.
 * <p>
 * Interview angle: stale version → {@link com.amazon.lld.cart.CartVersionException}.
 */
public class CartScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Cart Optimistic Locking ---");
        var cart = fx.alice.getCart();
        cart.clear();

        int v = cart.getVersion();
        new AddItemToCartCommand(cart,
                new Item(fx.phone.getId(), 1, fx.phone.getPrice()), v).execute();
        System.out.println("Added phone, version=" + cart.getVersion() + ", total=$" + cart.total());

        try {
            cart.addItem(new Item(fx.book.getId(), 1, fx.book.getPrice()), v);
        } catch (CartVersionException e) {
            System.out.println("Stale version caught: " + e.getMessage());
        }

        cart.addItem(new Item(fx.book.getId(), 1, fx.book.getPrice()), cart.getVersion());
        System.out.println("Added book with fresh version, items=" + cart.getItems().size());
        cart.clear();
    }
}
