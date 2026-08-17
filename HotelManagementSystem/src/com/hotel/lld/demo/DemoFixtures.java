package com.hotel.lld.demo;

import com.hotel.lld.account.Guest;
import com.hotel.lld.billing.BillingService;
import com.hotel.lld.booking.BookingService;
import com.hotel.lld.booking.FullRefundBefore24HoursPolicy;
import com.hotel.lld.events.AsyncEventBus;
import com.hotel.lld.events.EmailService;
import com.hotel.lld.events.HotelEventType;
import com.hotel.lld.events.PushNotificationService;
import com.hotel.lld.events.SMSService;
import com.hotel.lld.hotel.Hotel;
import com.hotel.lld.payment.CashPaymentGateway;
import com.hotel.lld.payment.CheckPaymentGateway;
import com.hotel.lld.payment.CreditCardPaymentGateway;
import com.hotel.lld.payment.PaymentMethod;
import com.hotel.lld.payment.PaymentService;
import com.hotel.lld.room.Room;
import com.hotel.lld.room.RoomInventory;
import com.hotel.lld.room.RoomStyle;
import com.hotel.lld.service.Amenity;
import com.hotel.lld.service.HousekeepingWorkflow;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Shared sample data and wired services for all demos.
 */
public final class DemoFixtures {
    public final Hotel downtown;
    public final Guest alice;
    public final Guest bob;

    public final Room standard101;
    public final Room deluxe201;
    public final Room suite301;

    public final Amenity lateCheckout;
    public final Amenity extraPillow;

    public final RoomInventory inventory;
    public final AsyncEventBus eventBus;
    public final BillingService billingService;
    public final PaymentService paymentService;
    public final HousekeepingWorkflow housekeeping;
    public final BookingService bookingService;

    public final Map<String, Guest> guests = new HashMap<>();

    public DemoFixtures() {
        downtown = new Hotel("HTL-001", "Harbor View Hotel", "Seattle", "500 Waterfront Ave");

        alice = new Guest("G-001", "Alice Guest", "alice@example.com", "+1-206-555-0101");
        bob = new Guest("G-002", "Bob Guest", "bob@example.com", "+1-206-555-0102");
        guests.put(alice.getGuestId(), alice);
        guests.put(bob.getGuestId(), bob);

        inventory = new RoomInventory();
        LocalDate seedFrom = LocalDate.now();
        LocalDate seedTo = seedFrom.plusMonths(6);

        standard101 = new Room("101", downtown.getHotelId(), RoomStyle.STANDARD, 120.0, false);
        deluxe201 = new Room("201", downtown.getHotelId(), RoomStyle.DELUXE, 200.0, false);
        suite301 = new Room("301", downtown.getHotelId(), RoomStyle.FAMILY_SUITE, 350.0, false);

        for (Room room : new Room[]{standard101, deluxe201, suite301}) {
            room.seedAvailability(seedFrom, seedTo, true);
            inventory.add(room);
        }

        lateCheckout = new Amenity("AM-LATE", "Late Checkout", 40.0);
        extraPillow = new Amenity("AM-PILLOW", "Extra Pillow", 5.0);

        eventBus = new AsyncEventBus();
        EmailService email = new EmailService();
        SMSService sms = new SMSService();
        PushNotificationService push = new PushNotificationService();

        for (HotelEventType type : HotelEventType.values()) {
            eventBus.subscribe(type, email);
            eventBus.subscribe(type, sms);
            eventBus.subscribe(type, push);
        }

        billingService = new BillingService();
        paymentService = new PaymentService();
        paymentService.registerGateway(PaymentMethod.CREDIT_CARD, new CreditCardPaymentGateway());
        paymentService.registerGateway(PaymentMethod.CHECK, new CheckPaymentGateway());
        paymentService.registerGateway(PaymentMethod.CASH, new CashPaymentGateway());

        housekeeping = new HousekeepingWorkflow(inventory);
        bookingService = new BookingService(
                inventory, billingService, eventBus,
                new FullRefundBefore24HoursPolicy(), housekeeping);
    }
}
