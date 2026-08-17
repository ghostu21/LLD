package com.hotel.lld.payment;

/** Simulated credit-card gateway. */
public class CreditCardPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            return PaymentResult.failure("Invalid amount");
        }
        return PaymentResult.success("Card charged $" + String.format("%.2f", request.getAmount()));
    }
}
