package com.amazon.lld.returns;

import java.util.UUID;

/**
 * Customer return request tied to an order.
 * <p>
 * Why: returns are a separate workflow from order fulfillment — tracked with
 * their own status and refund amount.
 */
public class ReturnRequest {
    private final String returnId;
    private final String orderId;
    private final ReturnReason reason;
    private ReturnStatus status;
    private final double refundAmount;

    /**
     * @param orderId      original order
     * @param reason       why returning
     * @param refundAmount amount to refund if approved
     */
    public ReturnRequest(String orderId, ReturnReason reason, double refundAmount) {
        this.returnId = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.reason = reason;
        this.status = ReturnStatus.REQUESTED;
        this.refundAmount = refundAmount;
    }

    /** @return return request id */
    public String getReturnId() { return returnId; }

    /** @return order id */
    public String getOrderId() { return orderId; }

    /** @return return reason */
    public ReturnReason getReason() { return reason; }

    /** @return workflow status */
    public ReturnStatus getStatus() { return status; }

    /** @return refund amount */
    public double getRefundAmount() { return refundAmount; }

    /** Updates return workflow status. */
    public void setStatus(ReturnStatus status) { this.status = status; }
}
