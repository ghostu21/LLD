package com.amazon.lld.demo;

import com.amazon.lld.account.Address;
import com.amazon.lld.account.Guest;
import com.amazon.lld.account.GuestFactory;
import com.amazon.lld.account.Member;
import com.amazon.lld.account.MemberFactory;
import com.amazon.lld.account.UserRole;
import com.amazon.lld.catalog.Product;
import com.amazon.lld.catalog.ProductCatalog;
import com.amazon.lld.catalog.ProductCategoryType;
import com.amazon.lld.events.AsyncEventBus;
import com.amazon.lld.events.NotificationService;
import com.amazon.lld.inventory.InventoryService;
import com.amazon.lld.order.CheckoutService;
import com.amazon.lld.order.OrderService;
import com.amazon.lld.payment.PaymentProcessor;
import com.amazon.lld.returns.ReturnService;

/**
 * Shared sample data and wired services for all demos.
 * <p>
 * Why: scenarios need consistent Alice (seller+buyer), guest, products, and
 * shared event bus / checkout stack.
 */
public final class DemoFixtures {
    public final Member alice;
    public final Guest guest;
    public final Product phone;
    public final Product book;
    public final Product shirt;

    public final ProductCatalog catalog;
    public final InventoryService inventory;
    public final AsyncEventBus eventBus;
    public final NotificationService notifications;
    public final PaymentProcessor paymentProcessor;
    public final CheckoutService checkoutService;
    public final OrderService orderService;
    public final ReturnService returnService;

    /** Builds Alice, guest, three products, and wired services. */
    public DemoFixtures() throws Exception {
        Address aliceAddr = new Address("123 Main St", "Seattle", "WA", "98101", "US");

        alice = new MemberFactory("alice", "secret123", UserRole.SELLER,
                "Alice Seller", "alice@example.com", aliceAddr).create();
        guest = new GuestFactory().create();

        catalog = new ProductCatalog();
        inventory = new InventoryService();
        eventBus = new AsyncEventBus();
        notifications = new NotificationService();
        paymentProcessor = new PaymentProcessor();

        eventBus.subscribe(com.amazon.lld.events.OrderEventType.ORDER_PLACED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.ORDER_SHIPPED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.ORDER_CANCELED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.SHIPMENT_UPDATED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.RETURN_REQUESTED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.REFUND_COMPLETED, notifications);
        eventBus.subscribe(com.amazon.lld.events.OrderEventType.ORDER_PLACED, alice);

        checkoutService = new CheckoutService(paymentProcessor, inventory, eventBus);
        orderService = new OrderService(checkoutService.getOrders(), inventory, eventBus);
        returnService = new ReturnService(eventBus);

        phone = new Product("Smartphone X", "Latest smartphone", 699.99,
                ProductCategoryType.ELECTRONICS, 50, alice.getMemberId());
        book = new Product("Clean Code", "Software craftsmanship", 39.99,
                ProductCategoryType.BOOKS, 100, alice.getMemberId());
        shirt = new Product("Cotton T-Shirt", "Comfortable tee", 19.99,
                ProductCategoryType.CLOTHING, 200, alice.getMemberId());

        for (Product p : new Product[]{phone, book, shirt}) {
            catalog.addProduct(p);
            inventory.setStock(p.getId(), p.getStockCount());
        }
    }
}
