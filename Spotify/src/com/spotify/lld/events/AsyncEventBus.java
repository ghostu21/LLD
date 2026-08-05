package com.spotify.lld.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * In-process async pub/sub bus (Observer done correctly).
 * <p>
 * Why: claiming "Observer" without async fan-out is empty. Notifications for
 * likes/releases must not block the publish/play path.
 * <p>
 * Logic: listeners register per {@link MusicEvent.Type}; {@link #publish}
 * snapshots subscribers and runs each {@code onEvent} on a thread-pool worker,
 * swallowing listener exceptions so one bad subscriber cannot kill others.
 */
public class AsyncEventBus {
    private final Map<MusicEvent.Type, List<MusicEventListener>> subscribers =
            new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /** Registers {@code listener} for events of {@code type}. */
    public void subscribe(MusicEvent.Type type, MusicEventListener listener) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /** Removes a previously registered listener (no-op if absent). */
    public void unsubscribe(MusicEvent.Type type, MusicEventListener listener) {
        subscribers.getOrDefault(type, List.of()).remove(listener);
    }

    /**
     * Fans out {@code event} to all subscribers asynchronously.
     * Logic: for each listener, executor.submit(onEvent); errors logged, not rethrown.
     */
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

    /** Stops the fan-out thread pool. */
    public void shutdown() {
        executor.shutdownNow();
    }
}
