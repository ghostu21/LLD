package com.vending.lld.inventory;

import com.vending.lld.catalog.Item;

/**
 * Stock-level state for a slot: Available → LowStock → OutOfStock.
 * <p>
 * Why: quantity checks scattered in {@code completeTransaction} miss reload,
 * expiry, and “almost empty” alerts. The state object owns those rules.
 */
public interface StockState {
    boolean canDispense(InventorySlot slot);

    StockState afterDispense(InventorySlot slot);

    StockState afterRestock(InventorySlot slot);

    String label();
}
