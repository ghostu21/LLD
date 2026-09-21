package com.vending.lld.command;

import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

/**
 * Return remaining balance to the customer.
 */
public final class RefundCommand implements PaymentCommand {
    private final PaymentStrategy strategy;
    private Money refunded = Money.zero();

    public RefundCommand(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public Money getRefunded() {
        return refunded;
    }

    @Override
    public void execute() {
        refunded = strategy.getBalance();
        strategy.resetBalance();
    }

    @Override
    public void undo() {
        strategy.addPayment(refunded);
        refunded = Money.zero();
    }

    @Override
    public String description() {
        return "REFUND " + refunded + " via " + strategy.name();
    }
}
