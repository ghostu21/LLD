package com.vending.lld.payment;

public final class CoinPayment extends PaymentStrategy {
    @Override
    public String name() {
        return "COIN";
    }
}
