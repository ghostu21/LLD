package com.vending.lld.hardware;

import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

public final class CardReader {
    public void authorize(Money amount, PaymentStrategy payment) {
        payment.addPayment(amount);
    }
}
