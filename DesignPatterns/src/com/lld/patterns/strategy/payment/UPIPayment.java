package com.lld.patterns.strategy.payment;

public class UPIPayment implements PaymentStrategy {
    private final String upiId;

    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public void pay(double amount) {
        System.out.println("Paid $" + amount + " using UPI ID " + upiId);
    }
}
