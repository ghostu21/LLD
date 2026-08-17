package com.hotel.lld.payment;

/** Simulated cash payment at front desk. */
public class CashPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.getAmount() <= 0) {
            return PaymentResult.failure("Invalid amount");
        }
        return PaymentResult.success("Cash received $" + String.format("%.2f", request.getAmount()));
    }
}
