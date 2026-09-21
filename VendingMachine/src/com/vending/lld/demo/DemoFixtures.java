package com.vending.lld.demo;

import com.vending.lld.admin.AdminAuth;
import com.vending.lld.catalog.ItemFactory;
import com.vending.lld.events.AlertListener;
import com.vending.lld.events.AsyncEventBus;
import com.vending.lld.events.AuditLogListener;
import com.vending.lld.events.DisplayListener;
import com.vending.lld.machine.VendingMachine;

import java.time.Instant;

/**
 * Wired machine with snack S1 ($1.50) and drink D1 ($2.50).
 */
public final class DemoFixtures {
    public final AsyncEventBus bus = new AsyncEventBus();
    public final DisplayListener display = new DisplayListener();
    public final AuditLogListener audit = new AuditLogListener();
    public final AlertListener alerts = new AlertListener();
    public final VendingMachine machine;

    public DemoFixtures() {
        bus.subscribeAll(display);
        bus.subscribeAll(audit);
        bus.subscribeAll(alerts);
        machine = new VendingMachine(bus, new AdminAuth("1234"));
        machine.addItem(ItemFactory.createItem("snack", "S1", 150), 10);
        machine.addItem(ItemFactory.createItem("drink", "D1", 250), 5);
    }

    public void settle() throws InterruptedException {
        Thread.sleep(80);
    }

    public void close() {
        bus.shutdown();
    }

    public void addExpired(String code, int priceCents) {
        machine.addItem(ItemFactory.createItem("snack", code, priceCents), 3, 1,
                Instant.now().minusSeconds(60));
    }

    public void addLowQty(String code, int priceCents, int qty, int threshold) {
        machine.addItem(ItemFactory.createItem("drink", code, priceCents), qty, threshold,
                Instant.now().plusSeconds(86_400));
    }
}
