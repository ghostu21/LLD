package com.hotel.lld.billing;

/** Single line on an itemized bill. */
public class BillItem {
    private final BillItemType type;
    private final String description;
    private final double amount;

    public BillItem(BillItemType type, String description, double amount) {
        this.type = type;
        this.description = description;
        this.amount = amount;
    }

    public BillItemType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }
}
