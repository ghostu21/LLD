package com.vending.lld.machine;

import com.vending.lld.admin.AdminAuth;
import com.vending.lld.catalog.Item;
import com.vending.lld.command.TransactionLog;
import com.vending.lld.events.AsyncEventBus;
import com.vending.lld.events.VendingEvent;
import com.vending.lld.events.VendingEventType;
import com.vending.lld.hardware.BillAcceptor;
import com.vending.lld.hardware.CardReader;
import com.vending.lld.hardware.CoinAcceptor;
import com.vending.lld.hardware.Dispenser;
import com.vending.lld.inventory.Inventory;
import com.vending.lld.inventory.InventorySlot;
import com.vending.lld.money.Money;
import com.vending.lld.payment.PaymentStrategy;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One physical machine: inventory, hardware ports, payment strategy, state.
 * <p>
 * Customer methods take {@code machineLock} so two people cannot share a
 * session. Payment cents are still atomic for the screenshot race.
 */
public final class VendingMachine {
    private final Inventory inventory = new Inventory();
    private final CoinAcceptor coinAcceptor = new CoinAcceptor();
    private final BillAcceptor billAcceptor = new BillAcceptor();
    private final CardReader cardReader = new CardReader();
    private final Dispenser dispenser = new Dispenser();
    private final AsyncEventBus eventBus;
    private final TransactionLog transactionLog = new TransactionLog();
    private final AdminAuth adminAuth;
    private final ReentrantLock machineLock = new ReentrantLock();

    private MachineState state = new IdleState();
    private PaymentStrategy payment;
    private String selectedCode;
    private int discountPercent;

    public VendingMachine(AsyncEventBus eventBus, AdminAuth adminAuth) {
        this.eventBus = eventBus;
        this.adminAuth = adminAuth;
    }

    public void addItem(Item item, int quantity) {
        addItem(item, quantity, 2, Instant.now().plusSeconds(86_400));
    }

    public void addItem(Item item, int quantity, int lowStockThreshold, Instant expiresAt) {
        inventory.addItem(item, quantity, lowStockThreshold, expiresAt);
    }

    public void selectItem(String code) {
        runLocked(() -> state.selectItem(this, code));
    }

    public void insertCoin(int cents) {
        runLocked(() -> state.insertCoin(this, cents));
    }

    public void insertBill(int dollars) {
        runLocked(() -> state.insertBill(this, dollars));
    }

    public void insertCard(Money amount) {
        runLocked(() -> state.chargeCard(this, amount));
    }

    public void completeTransaction() {
        runLocked(() -> state.complete(this));
    }

    public void cancelTransaction() {
        runLocked(() -> state.cancel(this));
    }

    public void enterMaintenance(String pin) {
        runLocked(() -> state.enterMaintenance(this, pin));
    }

    public void exitMaintenance() {
        runLocked(() -> state.exitMaintenance(this));
    }

    public void restock(String code, int added, Instant expiry) {
        runLocked(() -> {
            requireMaintenance();
            InventorySlot slot = inventory.get(code);
            if (slot == null) {
                throw new IllegalArgumentException("Unknown " + code);
            }
            slot.restock(added, expiry);
            publish(new VendingEvent(VendingEventType.MAINTENANCE,
                    "Restocked " + code + " qty=" + slot.getQuantity()));
        });
    }

    public void setPrice(String code, Money price) {
        runLocked(() -> {
            requireMaintenance();
            InventorySlot slot = inventory.get(code);
            slot.getItem().setPrice(price);
            publish(new VendingEvent(VendingEventType.PRICE_CHANGED, code + " -> " + price));
        });
    }

    public void applyDiscountPercent(int percent) {
        runLocked(() -> discountPercent = percent);
    }

    public Money priceFor(InventorySlot slot) {
        Money base = slot.getItem().getPrice();
        return discountPercent <= 0 ? base : base.percentOff(discountPercent);
    }

    public Inventory inventory() {
        return inventory;
    }

    public InventorySlot slot(String code) {
        return inventory.get(code);
    }

    public MachineState getState() {
        return state;
    }

    public String stateName() {
        return state.name();
    }

    public PaymentStrategy payment() {
        return payment;
    }

    public TransactionLog transactionLog() {
        return transactionLog;
    }

    public AsyncEventBus eventBus() {
        return eventBus;
    }

    void setState(MachineState state) {
        this.state = state;
    }

    void setPaymentStrategy(PaymentStrategy payment) {
        this.payment = payment;
    }

    public void setPaymentStrategyPublic(PaymentStrategy payment) {
        runLocked(() -> this.payment = payment);
    }

    CoinAcceptor coinAcceptor() {
        return coinAcceptor;
    }

    BillAcceptor billAcceptor() {
        return billAcceptor;
    }

    CardReader cardReader() {
        return cardReader;
    }

    Dispenser dispenser() {
        return dispenser;
    }

    AdminAuth adminAuth() {
        return adminAuth;
    }

    String getSelectedCode() {
        return selectedCode;
    }

    void setSelectedCode(String selectedCode) {
        this.selectedCode = selectedCode;
    }

    void clearSelection() {
        selectedCode = null;
    }

    void clearDiscount() {
        discountPercent = 0;
    }

    void publish(VendingEvent event) {
        eventBus.publish(event);
    }

    private void requireMaintenance() {
        if (!(state instanceof MaintenanceState)) {
            throw new IllegalStateException("Admin ops require maintenance mode");
        }
    }

    private void runLocked(Runnable action) {
        machineLock.lock();
        try {
            action.run();
        } finally {
            machineLock.unlock();
        }
    }
}
