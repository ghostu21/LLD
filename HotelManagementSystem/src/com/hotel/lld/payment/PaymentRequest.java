package com.hotel.lld.payment;

/** Charge request sent to a gateway. */
public class PaymentRequest {
    private final String paymentId;
    private final String reservationNumber;
    private final double amount;
    private final PaymentMethod method;

    public PaymentRequest(String paymentId, String reservationNumber,
                          double amount, PaymentMethod method) {
        this.paymentId = paymentId;
        this.reservationNumber = reservationNumber;
        this.amount = amount;
        this.method = method;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }
}
