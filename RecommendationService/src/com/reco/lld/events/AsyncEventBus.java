package com.reco.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process async pub/sub (Observer).
 * <p>
 * Why: generating a slate must not block on email/push; feedback ingestion
 * fans out without coupling the write path to listeners.
 */
public class AsyncEventBus {
    private final Map<RecoEventType, List<RecoEventListener>> subscribers = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void subscribe(RecoEventType type, RecoEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public void publish(RecoEvent event) {
        for (RecoEventListener listener : subscribers.getOrDefault(event.getType(), List.of())) {
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
