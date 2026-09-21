package com.vending.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Async fan-out so display, audit log, and low-stock alerts are not hardcoded
 * into {@code VendingMachine}.
 */
public final class AsyncEventBus {
    private final Map<VendingEventType, List<VendingEventListener>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "vending-events");
        t.setDaemon(true);
        return t;
    });

    public void subscribe(VendingEventType type, VendingEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void subscribeAll(VendingEventListener listener) {
        for (VendingEventType type : VendingEventType.values()) {
            subscribe(type, listener);
        }
    }

    public void publish(VendingEvent event) {
        for (VendingEventListener listener : subscribers.getOrDefault(event.getType(), List.of())) {
            executor.submit(() -> listener.onEvent(event));
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
