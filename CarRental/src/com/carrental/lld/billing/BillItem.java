package com.carrental.lld.billing;

/**
 * Single line on an itemized rental bill.
 */
public class BillItem {
    private final BillItemType type;
    private final String description;
    private final double amount;

    /**
     * @param type        line category
     * @param description human-readable label
     * @param amount      monetary amount
     */
    public BillItem(BillItemType type, String description, double amount) {
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    /** @return item type */
    public BillItemType getType() { return type; }

    /** @return description */
    public String getDescription() { return description; }

    /** @return amount */
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("  [%s] %s: $%.2f", type, description, amount);
    }
}
