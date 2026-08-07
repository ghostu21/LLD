package com.carrental.lld.payment;

/**
 * Payment charge request payload.
 */
public class PaymentRequest {
    private final String reservationNumber;
    private final String memberId;
    private final double amount;
    private final PaymentMethod method;

    /**
     * @param reservationNumber linked reservation
     * @param memberId          payer member id
     * @param amount            charge amount
     * @param method            payment rail
     */
    public PaymentRequest(String reservationNumber, String memberId,
                          double amount, PaymentMethod method) {
        this.reservationNumber = reservationNumber;
        this.memberId = memberId;
        this.amount = amount;
        this.method = method;
    }

    /** @return reservation number */
    public String getReservationNumber() { return reservationNumber; }

    /** @return member id */
    public String getMemberId() { return memberId; }

    /** @return amount to charge */
    public double getAmount() { return amount; }

    /** @return payment method */
    public PaymentMethod getMethod() { return method; }
}
