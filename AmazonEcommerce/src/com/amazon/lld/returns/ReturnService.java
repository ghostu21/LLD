package com.amazon.lld.returns;

import com.amazon.lld.events.AsyncEventBus;
import com.amazon.lld.events.OrderEvent;
import com.amazon.lld.events.OrderEventType;
import com.amazon.lld.order.Order;
import com.amazon.lld.order.OrderStatus;
import com.amazon.lld.payment.PaymentStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages return requests, approvals, and refund completion.
 * <p>
 * Why: encapsulates return workflow and publishes domain events on state changes.
 * <p>
 * Logic: requestReturn → approve → completeRefund updates order + payment status.
 */
public class ReturnService {
    private final Map<String, ReturnRequest> returns = new ConcurrentHashMap<>();
    private final Map<String, Refund> refunds = new ConcurrentHashMap<>();
    private final AsyncEventBus eventBus;

    /**
     * @param eventBus optional bus for RETURN_REQUESTED / REFUND_COMPLETED
     */
    public ReturnService(AsyncEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Submits a return for an order.
     *
     * @param order  order to return (must be shipped/delivered)
     * @param reason customer reason
     * @return created return request
     */
    public ReturnRequest requestReturn(Order order, ReturnReason reason) {
        order.requestReturn();
        double amount = order.getPayment().getAmount();
        ReturnRequest request = new ReturnRequest(order.getOrderId(), reason, amount);
        returns.put(request.getReturnId(), request);
        publish(OrderEventType.RETURN_REQUESTED, order,
                "Return requested: " + reason);
        return request;
    }

    /**
     * Approves a pending return request.
     *
     * @param returnId return request id
     */
    public void approve(String returnId) {
        ReturnRequest request = returns.get(returnId);
        if (request == null) throw new IllegalArgumentException("Unknown return: " + returnId);
        request.setStatus(ReturnStatus.APPROVED);
    }

    /**
     * Completes refund: marks return REFUNDED, order REFUND_APPLIED, payment REFUNDED.
     *
     * @param returnId return request id
     * @param order    associated order (caller provides)
     * @return refund record
     */
    public Refund completeRefund(String returnId, Order order) {
        ReturnRequest request = returns.get(returnId);
        if (request == null) throw new IllegalArgumentException("Unknown return: " + returnId);
        request.setStatus(ReturnStatus.REFUNDED);
        order.setStatus(OrderStatus.REFUND_APPLIED);
        order.getPayment().setStatus(PaymentStatus.REFUNDED);
        Refund refund = new Refund(returnId, request.getRefundAmount());
        refund.setStatus(PaymentStatus.REFUNDED);
        refunds.put(refund.getRefundId(), refund);
        publish(OrderEventType.REFUND_COMPLETED, order,
                "$" + refund.getAmount() + " refunded");
        return refund;
    }

    private void publish(OrderEventType type, Order order, String payload) {
        if (eventBus != null) {
            eventBus.publish(new OrderEvent(type, order.getOrderId(),
                    order.getMemberId(), payload));
        }
    }
}
