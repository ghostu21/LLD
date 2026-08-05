package com.spotify.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncEventBus {
    private final Map<MusicEvent.Type, List<MusicEventListener>> subscribers =
            new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void subscribe(MusicEvent.Type type, MusicEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void unsubscribe(MusicEvent.Type type, MusicEventListener listener) {
        subscribers.getOrDefault(type, List.of()).remove(listener);
    }

    public void publish(MusicEvent event) {
        List<MusicEventListener> listeners =
                subscribers.getOrDefault(event.type, List.of());

        for (MusicEventListener listener : listeners) {
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
