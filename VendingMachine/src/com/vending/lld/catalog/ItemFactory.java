package com.vending.lld.catalog;

import com.vending.lld.money.Money;

/**
 * Factory for catalog items so the machine does not switch on type strings
 * in the purchase path.
 */
public final class ItemFactory {
    private ItemFactory() {
    }

    public static Item createItem(String type, String code, int priceCents) {
        Money price = Money.ofCents(priceCents);
        return switch (type.toLowerCase()) {
            case "snack" -> new SnackItem(code, price);
            case "drink" -> new DrinkItem(code, price);
            default -> throw new IllegalArgumentException("Invalid item type: " + type);
        };
    }
}
