package com.vending.lld.catalog;

import com.vending.lld.money.Money;

/**
 * Sellable product. Price is mutable only in admin/maintenance.
 */
public abstract class Item {
    private final String code;
    private final ItemType type;
    private Money price;

    protected Item(String code, ItemType type, Money price) {
        this.code = code;
        this.type = type;
        this.price = price;
    }

    public String getCode() {
        return code;
    }

    public ItemType getType() {
        return type;
    }

    public Money getPrice() {
        return price;
    }

    public void setPrice(Money price) {
        this.price = price;
    }
}
