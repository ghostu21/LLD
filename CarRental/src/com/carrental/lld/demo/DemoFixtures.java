package com.carrental.lld.demo;

import com.carrental.lld.account.Member;
import com.carrental.lld.account.PasswordUtils;
import com.carrental.lld.addon.Equipment;
import com.carrental.lld.addon.EquipmentCatalog;
import com.carrental.lld.addon.InsuranceCatalog;
import com.carrental.lld.addon.InsuranceProduct;
import com.carrental.lld.addon.RentalInsurance;
import com.carrental.lld.addon.ServiceAddon;
import com.carrental.lld.addon.ServiceCatalog;
import com.carrental.lld.billing.BillingService;
import com.carrental.lld.branch.Branch;
import com.carrental.lld.events.AsyncEventBus;
import com.carrental.lld.events.NotificationService;
import com.carrental.lld.events.RentalEventType;
import com.carrental.lld.log.VehicleLogService;
import com.carrental.lld.payment.BankPaymentGateway;
import com.carrental.lld.payment.CardPaymentGateway;
import com.carrental.lld.payment.PaymentMethod;
import com.carrental.lld.payment.PaymentService;
import com.carrental.lld.reservation.ReservationService;
import com.carrental.lld.reservation.StandardCancellationPolicy;
import com.carrental.lld.vehicle.Vehicle;
import com.carrental.lld.vehicle.VehicleInventory;
import com.carrental.lld.vehicle.VehicleType;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared sample data and wired services for all demos.
 * <p>
 * Why: scenarios need consistent branches, members, vehicles, catalogs, and a
 * fully wired reservation/billing/payment/event stack.
 */
public final class DemoFixtures {
    public final Branch downtown;
    public final Branch airport;
    public final Member alice;
    public final Member bob;

    public final Vehicle sedan;
    public final Vehicle suv;
    public final Vehicle truck;

    public final EquipmentCatalog equipmentCatalog;
    public final ServiceCatalog serviceCatalog;
    public final InsuranceCatalog insuranceCatalog;

    public final VehicleInventory inventory;
    public final VehicleLogService logService;
    public final AsyncEventBus eventBus;
    public final NotificationService notifications;
    public final BillingService billingService;
    public final PaymentService paymentService;
    public final CardPaymentGateway cardGateway;
    public final ReservationService reservationService;

    public final Map<String, Branch> branches = new HashMap<>();

    /** Builds branches, members, vehicles, catalogs, and wired services. */
    public DemoFixtures() throws Exception {
        downtown = new Branch("BR-DTW", "Downtown Hub", "Seattle", "100 Pike St");
        airport = new Branch("BR-SEA", "SeaTac Airport", "SeaTac", "17801 International Blvd");
        branches.put(downtown.getBranchId(), downtown);
        branches.put(airport.getBranchId(), airport);

        String saltAlice = PasswordUtils.generateSalt();
        String hashAlice = PasswordUtils.hash("secret123", saltAlice);
        alice = new Member("M-001", "alice", hashAlice, saltAlice,
                "Alice Driver", "alice@example.com", "DL-ALICE-001");

        String saltBob = PasswordUtils.generateSalt();
        String hashBob = PasswordUtils.hash("drive456", saltBob);
        bob = new Member("M-002", "bob", hashBob, saltBob,
                "Bob Renter", "bob@example.com", "DL-BOB-002");

        inventory = new VehicleInventory();
        sedan = new Vehicle("VH-CAR-001", VehicleType.CAR, "Toyota", "Camry",
                "A-12", downtown.getBranchId(), 45.00, 10.00);
        suv = new Vehicle("VH-SUV-001", VehicleType.SUV, "Honda", "CR-V",
                "B-03", downtown.getBranchId(), 65.00, 15.00);
        truck = new Vehicle("VH-TRK-001", VehicleType.TRUCK, "Ford", "F-150",
                "C-07", airport.getBranchId(), 85.00, 20.00);

        for (Vehicle v : new Vehicle[]{sedan, suv, truck}) {
            inventory.add(v);
        }

        equipmentCatalog = new EquipmentCatalog();
        equipmentCatalog.add(new Equipment("GPS", "GPS Navigation", 8.00, false));
        equipmentCatalog.add(new Equipment("CHILD_SEAT", "Child Seat", 12.00, false));
        equipmentCatalog.add(new Equipment("SKI_RACK", "Ski Rack", 25.00, true));

        serviceCatalog = new ServiceCatalog();
        serviceCatalog.add(new ServiceAddon("ROADSIDE", "Roadside Assistance", 5.00, false));
        serviceCatalog.add(new ServiceAddon("EXTRA_DRIVER", "Additional Driver", 15.00, true));
        serviceCatalog.add(new ServiceAddon("WIFI", "Mobile WiFi Hotspot", 10.00, false));

        insuranceCatalog = new InsuranceCatalog();
        RentalInsurance collision = new RentalInsurance("POL-COL", "COLLISION", 500.0, 50000.0);
        insuranceCatalog.add(new InsuranceProduct("INS-COL", "Collision Damage Waiver", 18.00, false, collision));

        logService = new VehicleLogService();
        eventBus = new AsyncEventBus();
        notifications = new NotificationService();
        billingService = new BillingService();
        paymentService = new PaymentService();
        cardGateway = new CardPaymentGateway();
        paymentService.registerGateway(PaymentMethod.CARD, cardGateway);
        paymentService.registerGateway(PaymentMethod.BANK, new BankPaymentGateway());

        eventBus.subscribe(RentalEventType.RESERVATION_CONFIRMED, notifications);
        eventBus.subscribe(RentalEventType.RESERVATION_CANCELLED, notifications);
        eventBus.subscribe(RentalEventType.PICKUP_REMINDER, notifications);
        eventBus.subscribe(RentalEventType.DUE_REMINDER, notifications);
        eventBus.subscribe(RentalEventType.OVERDUE, notifications);
        eventBus.subscribe(RentalEventType.PAYMENT_COMPLETED, notifications);
        eventBus.subscribe(RentalEventType.RETURNED, notifications);

        reservationService = new ReservationService(
                inventory, billingService, logService, eventBus, new StandardCancellationPolicy());
    }
}
