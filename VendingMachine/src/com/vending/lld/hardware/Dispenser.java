package com.vending.lld.hardware;

import com.vending.lld.catalog.Item;

public final class Dispenser {
    public void dispenseItem(Item item) {
        System.out.println("Dispensing item: " + item.getCode());
    }
}
