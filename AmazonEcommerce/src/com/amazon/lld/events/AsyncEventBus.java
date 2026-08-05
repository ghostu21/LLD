package com.amazon.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process async pub/sub bus for order events.
 * <p>
 * Why: notifications must not block checkout or shipping paths — fan-out
 * runs on a thread pool (not sync SystemNotifier).
 * <p>
 * Logic: listeners register per {@link OrderEventType}; {@link #publish}
 * snapshots subscribers and runs each {@code onEvent} on a worker thread.
 */
public class AsyncEventBus {
    private final Map<OrderEventType, List<OrderEventListener>> subscribers =
            new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Registers a listener for events of the given type.
     *
     * @param type     event type
     * @param listener subscriber
     */
    public void subscribe(OrderEventType type, OrderEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Removes a previously registered listener.
     */
    public void unsubscribe(OrderEventType type, OrderEventListener listener) {
        subscribers.getOrDefault(type, List.of()).remove(listener);
    }

    /**
     * Fans out the event to all subscribers asynchronously.
     * Logic: executor.submit per listener; errors logged, not rethrown.
     */
    public void publish(OrderEvent event) {
        List<OrderEventListener> listeners =
                subscribers.getOrDefault(event.getType(), List.of());

        for (OrderEventListener listener : listeners) {
            executor.submit(() -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Listener error: " + e.getMessage());
                }
            });
        }
    }

    /** Stops the fan-out thread pool. */
    public void shutdown() {
        executor.shutdownNow();
    }
}
