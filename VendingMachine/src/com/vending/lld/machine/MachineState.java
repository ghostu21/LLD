package com.vending.lld.machine;

import com.vending.lld.money.Money;

/**
 * Machine-level state: which customer/admin operations are legal.
 */
public interface MachineState {
    void selectItem(VendingMachine machine, String code);

    void insertCoin(VendingMachine machine, int cents);

    void insertBill(VendingMachine machine, int dollars);

    void chargeCard(VendingMachine machine, Money amount);

    void complete(VendingMachine machine);

    void cancel(VendingMachine machine);

    void enterMaintenance(VendingMachine machine, String pin);

    void exitMaintenance(VendingMachine machine);

    String name();
}
