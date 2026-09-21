package com.vending.lld.machine;

import com.vending.lld.command.CollectPaymentCommand;
import com.vending.lld.command.RefundCommand;
import com.vending.lld.events.VendingEvent;
import com.vending.lld.events.VendingEventType;
import com.vending.lld.inventory.InventorySlot;
import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

final class CollectingState extends IllegalOpsState {
    @Override
    public void selectItem(VendingMachine machine, String code) {
        new IdleState().selectItem(machine, code);
    }

    @Override
    public void insertCoin(VendingMachine machine, int cents) {
        machine.coinAcceptor().acceptCoin(cents, machine.payment());
        machine.publish(new VendingEvent(VendingEventType.PAYMENT_RECEIVED,
                "Balance " + machine.payment().getBalance()));
    }

    @Override
    public void insertBill(VendingMachine machine, int dollars) {
        machine.billAcceptor().acceptBill(dollars, machine.payment());
        machine.publish(new VendingEvent(VendingEventType.PAYMENT_RECEIVED,
                "Balance " + machine.payment().getBalance()));
    }

    @Override
    public void chargeCard(VendingMachine machine, Money amount) {
        machine.cardReader().authorize(amount, machine.payment());
        machine.publish(new VendingEvent(VendingEventType.PAYMENT_RECEIVED,
                "Balance " + machine.payment().getBalance()));
    }

    @Override
    public void complete(VendingMachine machine) {
        String code = machine.getSelectedCode();
        if (code == null) {
            machine.publish(new VendingEvent(VendingEventType.ITEM_SELECTED, "No item selected"));
            return;
        }
        InventorySlot slot = machine.inventory().get(code);
        if (slot == null || !slot.canDispense()) {
            machine.publish(new VendingEvent(VendingEventType.OUT_OF_STOCK, "Cannot dispense " + code));
            return;
        }
        Money price = machine.priceFor(slot);
        PaymentStrategy payment = machine.payment();
        if (!payment.getBalance().ge(price)) {
            machine.publish(new VendingEvent(VendingEventType.INSUFFICIENT_FUNDS,
                    "Need " + price + " have " + payment.getBalance()));
            return;
        }
        CollectPaymentCommand collect = new CollectPaymentCommand(payment, price);
        collect.execute();
        machine.transactionLog().record(collect);
        if (!slot.tryDispense()) {
            collect.undo();
            machine.transactionLog().record("ROLLBACK " + collect.description());
            machine.publish(new VendingEvent(VendingEventType.OUT_OF_STOCK, "Lost race on " + code));
            return;
        }
        machine.dispenser().dispenseItem(slot.getItem());
        machine.publish(new VendingEvent(VendingEventType.DISPENSED, "Dispensed " + code));
        if (slot.getState().label().equals("LOW_STOCK")) {
            machine.publish(new VendingEvent(VendingEventType.LOW_STOCK, code + " low"));
        }
        if (slot.getState().label().equals("OUT_OF_STOCK")) {
            machine.publish(new VendingEvent(VendingEventType.OUT_OF_STOCK, code + " empty"));
        }
        RefundCommand change = new RefundCommand(payment);
        change.execute();
        machine.transactionLog().record(change);
        if (change.getRefunded().cents() > 0) {
            machine.publish(new VendingEvent(VendingEventType.REFUNDED, "Change " + change.getRefunded()));
        }
        machine.clearSelection();
        machine.clearDiscount();
        machine.setState(new IdleState());
    }

    @Override
    public void cancel(VendingMachine machine) {
        RefundCommand refund = new RefundCommand(machine.payment());
        refund.execute();
        machine.transactionLog().record(refund);
        machine.publish(new VendingEvent(VendingEventType.REFUNDED, "Refund " + refund.getRefunded()));
        machine.clearSelection();
        machine.clearDiscount();
        machine.setState(new IdleState());
    }

    @Override
    public String name() {
        return "COLLECTING";
    }
}
