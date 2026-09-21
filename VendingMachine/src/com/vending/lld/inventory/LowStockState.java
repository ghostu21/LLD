package com.vending.lld.inventory;

public final class LowStockState implements StockState {
    @Override
    public boolean canDispense(InventorySlot slot) {
        return slot.getQuantity() > 0 && !slot.isExpired();
    }

    @Override
    public StockState afterDispense(InventorySlot slot) {
        return slot.resolveState();
    }

    @Override
    public StockState afterRestock(InventorySlot slot) {
        return slot.resolveState();
    }

    @Override
    public String label() {
        return "LOW_STOCK";
    }
}
