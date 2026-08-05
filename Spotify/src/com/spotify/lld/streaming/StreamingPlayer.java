package com.spotify.lld.streaming;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StreamingPlayer {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean stopRequested = false;

    public Future<?> stream(StreamBuffer buffer, AudioDecoder decoder) {
        stopRequested = false;
        return executor.submit(() -> {
            while (buffer.hasMore() && !stopRequested) {
                byte[] chunk = buffer.nextChunk();
                decoder.decode(chunk);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void stop() {
        stopRequested = true;
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
