package com.lld.patterns.strategy.payment;

public class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;

    public CreditCardPayment(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    @Override
    public void pay(double amount) {
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        System.out.println("Paid $" + amount + " using credit card ending in " + last4);
    }
}
