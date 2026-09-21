package com.vending.lld.catalog;

import com.vending.lld.money.Money;

public final class SnackItem extends Item {
    public SnackItem(String code, Money price) {
        super(code, ItemType.SNACK, price);
    }
}
