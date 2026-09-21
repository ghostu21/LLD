package com.vending.lld.command;

import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

/**
 * Capture (debit) the sale amount; undo credits it back (refund).
 */
public final class CollectPaymentCommand implements PaymentCommand {
    private final PaymentStrategy strategy;
    private final Money amount;
    private boolean applied;

    public CollectPaymentCommand(PaymentStrategy strategy, Money amount) {
        this.strategy = strategy;
        this.amount = amount;
    }

    @Override
    public void execute() {
        applied = strategy.tryDebit(amount);
        if (!applied) {
            throw new IllegalStateException("Insufficient funds for " + amount);
        }
    }

    @Override
    public void undo() {
        if (applied) {
            strategy.addPayment(amount);
            applied = false;
        }
    }

    @Override
    public String description() {
        return "COLLECT " + amount + " via " + strategy.name();
    }
}
