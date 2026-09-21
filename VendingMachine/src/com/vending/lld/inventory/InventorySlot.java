package com.vending.lld.inventory;

import com.vending.lld.catalog.Item;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One coil/slot: quantity, expiry, low-stock threshold, and stock state.
 */
public final class InventorySlot {
    private final Item item;
    private final AtomicInteger quantity;
    private final int lowStockThreshold;
    private Instant expiresAt;
    private StockState state;

    public InventorySlot(Item item, int quantity, int lowStockThreshold, Instant expiresAt) {
        this.item = item;
        this.quantity = new AtomicInteger(quantity);
        this.lowStockThreshold = lowStockThreshold;
        this.expiresAt = expiresAt;
        this.state = resolveState();
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public StockState getState() {
        return state;
    }

    public boolean canDispense() {
        return state.canDispense(this);
    }

    /**
     * CAS decrement so two purchases cannot both take the last unit.
     */
    public boolean tryDispense() {
        while (true) {
            int current = quantity.get();
            if (current <= 0 || isExpired()) {
                state = resolveState();
                return false;
            }
            if (quantity.compareAndSet(current, current - 1)) {
                state = state.afterDispense(this);
                return true;
            }
        }
    }

    public void restock(int added, Instant newExpiry) {
        quantity.addAndGet(added);
        if (newExpiry != null) {
            expiresAt = newExpiry;
        }
        state = state.afterRestock(this);
    }

    public StockState resolveState() {
        if (quantity.get() <= 0 || isExpired()) {
            return new OutOfStockState();
        }
        if (quantity.get() <= lowStockThreshold) {
            return new LowStockState();
        }
        return new AvailableState();
    }
}
