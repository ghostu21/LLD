package com.carrental.lld.events;

/**
 * Example {@link RentalEventListener} routing events to notification channels.
 * <p>
 * Why: proves async Observer wiring end-to-end (subscribe → publish → fan-out).
 * <p>
 * Logic: switch on event type → push / SMS / email stubs (println in demo).
 */
public class NotificationService implements RentalEventListener {

    /**
     * Dispatches by type to the appropriate notification channel stub.
     */
    @Override
    public void onEvent(RentalEvent event) {
        switch (event.getType()) {
            case RESERVATION_CONFIRMED -> sendEmail(event.getMemberId(),
                    "Reservation confirmed: " + event.getPayload());
            case RESERVATION_CANCELLED -> sendEmail(event.getMemberId(),
                    "Reservation cancelled: " + event.getPayload());
            case PICKUP_REMINDER -> sendPush(event.getMemberId(),
                    "Pickup approaching: " + event.getPayload());
            case DUE_REMINDER -> sendPush(event.getMemberId(),
                    "Return due soon: " + event.getPayload());
            case OVERDUE -> sendSms(event.getMemberId(),
                    "Vehicle overdue: " + event.getPayload());
            case PAYMENT_COMPLETED -> sendEmail(event.getMemberId(),
                    "Payment received: " + event.getPayload());
            case RETURNED -> sendInApp(event.getMemberId(),
                    "Thank you for returning: " + event.getPayload());
            default -> {}
        }
    }

    private void sendPush(String memberId, String msg) {
        System.out.println("  [push] to " + memberId + ": " + msg);
    }

    private void sendInApp(String memberId, String msg) {
        System.out.println("  [in-app] to " + memberId + ": " + msg);
    }

    private void sendEmail(String memberId, String msg) {
        System.out.println("  [email] to " + memberId + ": " + msg);
    }

    private void sendSms(String memberId, String msg) {
        System.out.println("  [sms] to " + memberId + ": " + msg);
    }
}
