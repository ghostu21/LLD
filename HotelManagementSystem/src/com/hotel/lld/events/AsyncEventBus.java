package com.hotel.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process async event bus — loose coupling, async, extensible subscribers.
 */
public class AsyncEventBus {
    private final Map<HotelEventType, List<HotelEventListener>> subscribers =
            new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void subscribe(HotelEventType type, HotelEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void unsubscribe(HotelEventType type, HotelEventListener listener) {
        subscribers.getOrDefault(type, List.of()).remove(listener);
    }

    public void publish(HotelEvent event) {
        List<HotelEventListener> listeners =
                subscribers.getOrDefault(event.getType(), List.of());
        for (HotelEventListener listener : listeners) {
            executor.submit(() -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Listener error: " + e.getMessage());
                }
            });
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
