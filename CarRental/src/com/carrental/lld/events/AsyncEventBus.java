package com.carrental.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process async pub/sub bus for rental notifications.
 * <p>
 * Why: reservation confirm/pickup/due/overdue must not block the request path.
 * <p>
 * Logic: listeners register per {@link RentalEventType}; {@link #publish}
 * snapshots subscribers and runs each {@code onEvent} on a thread-pool worker,
 * swallowing listener exceptions so one bad subscriber cannot kill others.
 */
public class AsyncEventBus {
    private final Map<RentalEventType, List<RentalEventListener>> subscribers =
            new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Registers a listener for events of the given type.
     *
     * @param type     event type
     * @param listener subscriber
     */
    public void subscribe(RentalEventType type, RentalEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param type     event type
     * @param listener subscriber to remove
     */
    public void unsubscribe(RentalEventType type, RentalEventListener listener) {
        subscribers.getOrDefault(type, List.of()).remove(listener);
    }

    /**
     * Fans out the event to all subscribers asynchronously.
     *
     * @param event domain event
     */
    public void publish(RentalEvent event) {
        List<RentalEventListener> listeners =
                subscribers.getOrDefault(event.getType(), List.of());

        for (RentalEventListener listener : listeners) {
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
