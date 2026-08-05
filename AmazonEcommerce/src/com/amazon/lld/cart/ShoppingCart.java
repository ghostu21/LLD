package com.amazon.lld.cart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Member shopping cart with optimistic locking via a version field.
 * <p>
 * Why: two tabs or devices updating the same cart concurrently must detect
 * conflicts instead of last-write-wins data loss.
 * <p>
 * Logic: every mutating method checks {@code expectedVersion == version},
 * applies change, then increments version. Mismatch throws
 * {@link CartVersionException}.
 */
public class ShoppingCart {
    private final String ownerId;
    private final CopyOnWriteArrayList<Item> items = new CopyOnWriteArrayList<>();
    private int version;

    /**
     * @param ownerId member account id
     */
    public ShoppingCart(String ownerId) {
        this.ownerId = ownerId;
    }

    /** @return cart owner (member id) */
    public String getOwnerId() { return ownerId; }

    /** @return current optimistic-lock version */
    public int getVersion() { return version; }

    /** @return unmodifiable view of line items */
    public List<Item> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Adds or merges a line item after version check.
     *
     * @param item            line to add
     * @param expectedVersion client-known version
     * @throws CartVersionException on stale version
     */
    public synchronized void addItem(Item item, int expectedVersion) {
        checkVersion(expectedVersion);
        for (Item existing : items) {
            if (existing.getProductId().equals(item.getProductId())) {
                existing.updateQuantity(existing.getQuantity() + item.getQuantity());
                version++;
                return;
            }
        }
        items.add(item);
        version++;
    }

    /**
     * Removes a product line after version check.
     *
     * @param productId       product to remove
     * @param expectedVersion client-known version
     * @throws CartVersionException on stale version
     */
    public synchronized void removeItem(String productId, int expectedVersion) {
        checkVersion(expectedVersion);
        items.removeIf(i -> i.getProductId().equals(productId));
        version++;
    }

    /**
     * Updates quantity for a product line after version check.
     *
     * @param productId       product id
     * @param newQuantity     new quantity
     * @param expectedVersion client-known version
     * @throws CartVersionException on stale version
     */
    public synchronized void updateQuantity(String productId, int newQuantity, int expectedVersion) {
        checkVersion(expectedVersion);
        for (Item item : items) {
            if (item.getProductId().equals(productId)) {
                item.updateQuantity(newQuantity);
                version++;
                return;
            }
        }
        throw new IllegalArgumentException("Product not in cart: " + productId);
    }

    /**
     * Clears all items and bumps version (used after successful checkout).
     */
    public synchronized void clear() {
        items.clear();
        version++;
    }

    /** @return sum of line totals */
    public double total() {
        return items.stream().mapToDouble(Item::lineTotal).sum();
    }

    private void checkVersion(int expectedVersion) {
        if (expectedVersion != version) {
            throw new CartVersionException(expectedVersion, version);
        }
    }
}
