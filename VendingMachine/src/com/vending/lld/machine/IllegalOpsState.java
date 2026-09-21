package com.vending.lld.machine;

import com.vending.lld.money.Money;

abstract class IllegalOpsState implements MachineState {
    @Override
    public void selectItem(VendingMachine machine, String code) {
        throw new IllegalStateException("selectItem not allowed in " + name());
    }

    @Override
    public void insertCoin(VendingMachine machine, int cents) {
        throw new IllegalStateException("insertCoin not allowed in " + name());
    }

    @Override
    public void insertBill(VendingMachine machine, int dollars) {
        throw new IllegalStateException("insertBill not allowed in " + name());
    }

    @Override
    public void chargeCard(VendingMachine machine, Money amount) {
        throw new IllegalStateException("chargeCard not allowed in " + name());
    }

    @Override
    public void complete(VendingMachine machine) {
        throw new IllegalStateException("complete not allowed in " + name());
    }

    @Override
    public void cancel(VendingMachine machine) {
        throw new IllegalStateException("cancel not allowed in " + name());
    }

    @Override
    public void enterMaintenance(VendingMachine machine, String pin) {
        throw new IllegalStateException("enterMaintenance not allowed in " + name());
    }

    @Override
    public void exitMaintenance(VendingMachine machine) {
        throw new IllegalStateException("exitMaintenance not allowed in " + name());
    }
}
