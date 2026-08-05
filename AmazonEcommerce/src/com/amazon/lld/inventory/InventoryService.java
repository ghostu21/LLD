package com.amazon.lld.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized stock reservation with thread-safe decrement.
 * <p>
 * Why: checkout must atomically reserve inventory to prevent overselling when
 * multiple buyers hit the same SKU concurrently.
 * <p>
 * Logic: {@link #reserve} synchronizes per-product decrement; {@link #release}
 * restores stock on cancel.
 */
public class InventoryService {
    private final Map<String, Integer> stock = new ConcurrentHashMap<>();

    /**
     * Seeds stock for a product (typically from catalog on startup).
     *
     * @param productId product id
     * @param quantity  available units
     */
    public void setStock(String productId, int quantity) {
        stock.put(productId, quantity);
    }

    /**
     * @param productId product id
     * @return current available stock
     */
    public int getStock(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    /**
     * Atomically reserves quantity if sufficient stock exists.
     *
     * @param productId product id
     * @param quantity  units to reserve
     * @return true if reservation succeeded
     */
    public synchronized boolean reserve(String productId, int quantity) {
        int available = stock.getOrDefault(productId, 0);
        if (available < quantity) {
            return false;
        }
        stock.put(productId, available - quantity);
        return true;
    }

    /**
     * Releases reserved stock back (order cancel).
     *
     * @param productId product id
     * @param quantity  units to restore
     */
    public synchronized void release(String productId, int quantity) {
        stock.merge(productId, quantity, Integer::sum);
    }
}
