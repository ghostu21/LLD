package com.vending.lld.payment;

public final class CardPayment extends PaymentStrategy {
    @Override
    public String name() {
        return "CARD";
    }
}
