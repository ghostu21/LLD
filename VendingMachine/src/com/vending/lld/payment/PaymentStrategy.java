package com.vending.lld.payment;

import com.vending.lld.money.Money;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Payment rail. Balance is an {@link AtomicInteger} of cents — Java has no
 * AtomicFloat, and {@code float +=} is both racy and imprecise.
 */
public abstract class PaymentStrategy {
    private final AtomicInteger balanceCents = new AtomicInteger();

    public void addPayment(Money amount) {
        balanceCents.addAndGet(amount.cents());
    }

    public Money getBalance() {
        return Money.ofCents(balanceCents.get());
    }

    public void resetBalance() {
        balanceCents.set(0);
    }

    /**
     * Atomically debit if funds cover {@code amount}.
     */
    public boolean tryDebit(Money amount) {
        while (true) {
            int current = balanceCents.get();
            if (current < amount.cents()) {
                return false;
            }
            if (balanceCents.compareAndSet(current, current - amount.cents())) {
                return true;
            }
        }
    }

    public abstract String name();
}
