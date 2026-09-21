package com.vending.lld.inventory;

public final class OutOfStockState implements StockState {
    @Override
    public boolean canDispense(InventorySlot slot) {
        return false;
    }

    @Override
    public StockState afterDispense(InventorySlot slot) {
        return this;
    }

    @Override
    public StockState afterRestock(InventorySlot slot) {
        return slot.resolveState();
    }

    @Override
    public String label() {
        return "OUT_OF_STOCK";
    }
}
