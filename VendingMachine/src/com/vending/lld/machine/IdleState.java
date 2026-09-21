package com.vending.lld.machine;

import com.vending.lld.events.VendingEvent;
import com.vending.lld.events.VendingEventType;
import com.vending.lld.inventory.InventorySlot;
import com.vending.lld.money.Money;
import com.vending.lld.payment.BillPayment;
import com.vending.lld.payment.CardPayment;
import com.vending.lld.payment.CoinPayment;

final class IdleState extends IllegalOpsState {
    @Override
    public void selectItem(VendingMachine machine, String code) {
        InventorySlot slot = machine.inventory().get(code);
        if (slot == null) {
            machine.publish(new VendingEvent(VendingEventType.ITEM_SELECTED, "Invalid item code: " + code));
            return;
        }
        if (!slot.canDispense()) {
            machine.publish(new VendingEvent(
                    slot.isExpired() ? VendingEventType.EXPIRED : VendingEventType.OUT_OF_STOCK,
                    code + " " + slot.getState().label()));
            return;
        }
        machine.setSelectedCode(code);
        Money price = machine.priceFor(slot);
        machine.publish(new VendingEvent(VendingEventType.ITEM_SELECTED,
                code + " price " + price + " stock=" + slot.getState().label()));
    }

    @Override
    public void insertCoin(VendingMachine machine, int cents) {
        machine.setPaymentStrategy(new CoinPayment());
        machine.setState(new CollectingState());
        machine.getState().insertCoin(machine, cents);
    }

    @Override
    public void insertBill(VendingMachine machine, int dollars) {
        machine.setPaymentStrategy(new BillPayment());
        machine.setState(new CollectingState());
        machine.getState().insertBill(machine, dollars);
    }

    @Override
    public void chargeCard(VendingMachine machine, Money amount) {
        machine.setPaymentStrategy(new CardPayment());
        machine.setState(new CollectingState());
        machine.getState().chargeCard(machine, amount);
    }

    @Override
    public void enterMaintenance(VendingMachine machine, String pin) {
        if (!machine.adminAuth().authenticate(pin)) {
            machine.publish(new VendingEvent(VendingEventType.MAINTENANCE, "Admin auth failed"));
            return;
        }
        machine.setState(new MaintenanceState());
        machine.publish(new VendingEvent(VendingEventType.MAINTENANCE, "Entered maintenance"));
    }

    @Override
    public String name() {
        return "IDLE";
    }
}
