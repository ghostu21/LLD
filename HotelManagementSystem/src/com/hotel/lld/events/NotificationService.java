package com.hotel.lld.events;

/**
 * Facade that fans hotel events to email / SMS / push channels.
 * Prefer registering channel services directly on the bus for true fan-out;
 * this class is a convenience aggregate listener for demos.
 */
public class NotificationService implements HotelEventListener {
    private final EmailService email = new EmailService();
    private final SMSService sms = new SMSService();
    private final PushNotificationService push = new PushNotificationService();

    @Override
    public void onEvent(HotelEvent event) {
        email.onEvent(event);
        sms.onEvent(event);
        push.onEvent(event);
    }
}
