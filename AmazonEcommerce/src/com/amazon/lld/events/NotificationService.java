package com.amazon.lld.events;

/**
 * Example {@link OrderEventListener} that routes events to notification channels.
 * <p>
 * Why: proves async Observer is wired end-to-end (subscribe → publish → fan-out).
 * <p>
 * Logic: switch on event type → push / email stubs (println in demo).
 */
public class NotificationService implements OrderEventListener {

    /**
     * Dispatches by type to push or email notification stubs.
     */
    @Override
    public void onEvent(OrderEvent event) {
        switch (event.getType()) {
            case ORDER_PLACED -> sendEmail(event.getMemberId(),
                    "Order " + event.getOrderId() + " confirmed: " + event.getPayload());
            case ORDER_SHIPPED -> sendPush(event.getMemberId(),
                    "Your order shipped! " + event.getPayload());
            case ORDER_CANCELED -> sendEmail(event.getMemberId(),
                    "Order canceled: " + event.getOrderId());
            case SHIPMENT_UPDATED -> sendPush(event.getMemberId(), event.getPayload());
            case RETURN_REQUESTED -> sendEmail(event.getMemberId(),
                    "Return requested for order " + event.getOrderId());
            case REFUND_COMPLETED -> sendEmail(event.getMemberId(),
                    "Refund completed: " + event.getPayload());
            default -> {}
        }
    }

    private void sendPush(String memberId, String msg) {
        System.out.println("  [push] to " + memberId + ": " + msg);
    }

    private void sendEmail(String memberId, String msg) {
        System.out.println("  [email] to " + memberId + ": " + msg);
    }
}
