package com.vending.lld.inventory;

import com.vending.lld.catalog.Item;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe catalog of slots keyed by item code.
 */
public final class Inventory {
    private final ConcurrentHashMap<String, InventorySlot> slots = new ConcurrentHashMap<>();

    public void put(InventorySlot slot) {
        slots.put(slot.getItem().getCode(), slot);
    }

    public InventorySlot get(String code) {
        return slots.get(code);
    }

    public Collection<InventorySlot> all() {
        return slots.values();
    }

    public void addItem(Item item, int quantity, int lowStockThreshold, Instant expiresAt) {
        put(new InventorySlot(item, quantity, lowStockThreshold, expiresAt));
    }
}
