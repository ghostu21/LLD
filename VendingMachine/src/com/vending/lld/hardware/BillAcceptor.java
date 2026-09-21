package com.vending.lld.hardware;

import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

public final class BillAcceptor {
    public void acceptBill(int dollarBills, PaymentStrategy payment) {
        if (dollarBills != 1 && dollarBills != 5 && dollarBills != 10) {
            throw new IllegalArgumentException("Rejected bill: $" + dollarBills);
        }
        payment.addPayment(Money.ofCents(dollarBills * 100));
    }
}
