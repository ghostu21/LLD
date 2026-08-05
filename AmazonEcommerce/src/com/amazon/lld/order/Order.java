package com.amazon.lld.order;

import com.amazon.lld.account.Address;
import com.amazon.lld.cart.Item;
import com.amazon.lld.payment.Payment;
import com.amazon.lld.shipping.ShipmentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Customer order with item snapshot, payment, shipping, and audit log.
 * <p>
 * Why: cart contents and address are frozen at checkout; status transitions
 * are guarded (e.g. cancel only before ship).
 * <p>
 * Logic: {@link #cancel} allowed for PENDING/UNSHIPPED; {@link #markShipped}
 * sets tracking; {@link #requestReturn} moves to RETURN_REQUESTED.
 */
public class Order {
    private final String orderId;
    private final String memberId;
    private final List<Item> items;
    private final Address shippingAddress;
    private OrderStatus status;
    private Payment payment;
    private String trackingNumber;
    private ShipmentStatus shipmentStatus;
    private final List<OrderLog> logs = new ArrayList<>();
    private final Instant createdAt;

    /**
     * @param memberId        buyer id
     * @param items           cart snapshot
     * @param shippingAddress delivery address
     * @param payment         completed payment
     */
    public Order(String memberId, List<Item> items, Address shippingAddress, Payment payment) {
        this.orderId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.items = new ArrayList<>(items);
        this.shippingAddress = shippingAddress;
        this.payment = payment;
        this.status = OrderStatus.UNSHIPPED;
        this.createdAt = Instant.now();
        addLog(OrderStatus.UNSHIPPED, "Order placed");
    }

    /** @return order id */
    public String getOrderId() { return orderId; }

    /** @return member id */
    public String getMemberId() { return memberId; }

    /** @return item snapshot */
    public List<Item> getItems() { return Collections.unmodifiableList(items); }

    /** @return shipping address */
    public Address getShippingAddress() { return shippingAddress; }

    /** @return current status */
    public OrderStatus getStatus() { return status; }

    /** @return payment record */
    public Payment getPayment() { return payment; }

    /** @return carrier tracking number (null until shipped) */
    public String getTrackingNumber() { return trackingNumber; }

    /** @return latest shipment status from carrier */
    public ShipmentStatus getShipmentStatus() { return shipmentStatus; }

    /** @return audit log entries */
    public List<OrderLog> getLogs() { return Collections.unmodifiableList(logs); }

    /** @return placement time */
    public Instant getCreatedAt() { return createdAt; }

    /**
     * Cancels order if not yet shipped.
     *
     * @throws IllegalStateException if already shipped or terminal
     */
    public void cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.UNSHIPPED) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }
        status = OrderStatus.CANCELED;
        addLog(OrderStatus.CANCELED, "Order canceled by customer");
    }

    /**
     * Marks order shipped with tracking number.
     *
     * @param trackingNumber carrier tracking id
     */
    public void markShipped(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
        this.shipmentStatus = ShipmentStatus.LABEL_CREATED;
        addLog(OrderStatus.SHIPPED, "Shipped with tracking " + trackingNumber);
    }

    /**
     * Updates carrier-reported shipment status (from poller).
     *
     * @param shipmentStatus new carrier status
     */
    public void updateShipmentStatus(ShipmentStatus shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
        if (shipmentStatus == ShipmentStatus.DELIVERED) {
            this.status = OrderStatus.DELIVERED;
            addLog(OrderStatus.DELIVERED, "Package delivered");
        } else {
            addLog(status, "Shipment update: " + shipmentStatus);
        }
    }

    /**
     * Customer initiates a return request.
     */
    public void requestReturn() {
        if (status != OrderStatus.DELIVERED && status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Return not allowed in status: " + status);
        }
        status = OrderStatus.RETURN_REQUESTED;
        addLog(OrderStatus.RETURN_REQUESTED, "Return requested");
    }

    /** Sets order status directly (used by ReturnService). */
    public void setStatus(OrderStatus status) {
        this.status = status;
        addLog(status, "Status updated to " + status);
    }

    /** Replaces payment record (e.g. after refund status change). */
    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    private void addLog(OrderStatus logStatus, String message) {
        logs.add(new OrderLog(logStatus, message));
    }
}
