package com.vending.lld.payment;

public final class BillPayment extends PaymentStrategy {
    @Override
    public String name() {
        return "BILL";
    }
}
