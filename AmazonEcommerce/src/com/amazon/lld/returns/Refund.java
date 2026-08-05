package com.amazon.lld.returns;

import com.amazon.lld.payment.PaymentStatus;

import java.util.UUID;

/**
 * Refund record linked to a {@link ReturnRequest}.
 * <p>
 * Why: refunds update {@link PaymentStatus} separately from return logistics.
 */
public class Refund {
    private final String refundId;
    private final String returnRequestId;
    private final double amount;
    private PaymentStatus status;

    /**
     * @param returnRequestId associated return
     * @param amount          refund amount
     */
    public Refund(String returnRequestId, double amount) {
        this.refundId = UUID.randomUUID().toString();
        this.returnRequestId = returnRequestId;
        this.amount = amount;
        this.status = PaymentStatus.REFUND_INITIATED;
    }

    /** @return refund id */
    public String getRefundId() { return refundId; }

    /** @return return request id */
    public String getReturnRequestId() { return returnRequestId; }

    /** @return refund amount */
    public double getAmount() { return amount; }

    /** @return payment/refund status */
    public PaymentStatus getStatus() { return status; }

    /** Updates refund status (e.g. REFUNDED). */
    public void setStatus(PaymentStatus status) { this.status = status; }
}
