package com.hotel.lld.payment;

/** Simulated check payment. */
public class CheckPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            return PaymentResult.failure("Invalid amount");
        }
        return PaymentResult.success("Check accepted $" + String.format("%.2f", request.getAmount()));
    }
}
