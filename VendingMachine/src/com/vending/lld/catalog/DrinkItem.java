package com.vending.lld.catalog;

import com.vending.lld.money.Money;

public final class DrinkItem extends Item {
    public DrinkItem(String code, Money price) {
        super(code, ItemType.DRINK, price);
    }
}
