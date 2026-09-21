package com.vending.lld.hardware;

import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

/**
 * Physical coin slot. Hardware classes stay thin; they only validate denomination
 * and forward cents to the payment strategy.
 */
public final class CoinAcceptor {
    public void acceptCoin(int cents, PaymentStrategy payment) {
        if (cents != 5 && cents != 10 && cents != 25 && cents != 100) {
            throw new IllegalArgumentException("Rejected coin: " + cents + "c");
        }
        payment.addPayment(Money.ofCents(cents));
    }
}
