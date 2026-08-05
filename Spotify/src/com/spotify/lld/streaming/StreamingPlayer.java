package com.spotify.lld.streaming;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Async consumer that pulls chunks from a {@link StreamBuffer} and feeds an
 * {@link AudioDecoder} (producer–consumer / backpressure demo).
 * <p>
 * Why: {@code song.play()} is not streaming. Real systems pace decode against
 * buffered chunks so the network producer cannot overwhelm the consumer.
 * <p>
 * Logic: {@link #stream} submits a loop on a single worker thread —
 * while buffer has data and stop not requested → nextChunk → decode → small
 * sleep (demo pacing). {@link #stop} flips a volatile flag; {@link #shutdown}
 * kills the executor.
 */
public class StreamingPlayer {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /** Volatile so stop() from another thread is visible to the worker. */
    private volatile boolean stopRequested = false;

    /**
     * Starts background streaming.
     * Logic: reset stop flag → submit loop that drains buffer through decoder.
     *
     * @return Future representing the streaming task (for join/cancel in tests)
     */
    public Future<?> stream(StreamBuffer buffer, AudioDecoder decoder) {
        stopRequested = false;
        return executor.submit(() -> {
            while (buffer.hasMore() && !stopRequested) {
                byte[] chunk = buffer.nextChunk();
                decoder.decode(chunk);
                try {
                    // Demo pacing — stands in for real decode/render time.
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /** Cooperative cancel: worker exits on next loop check. */
    public void stop() {
        stopRequested = true;
    }

    /** Hard shutdown of the worker pool (session teardown). */
    public void shutdown() {
        executor.shutdownNow();
    }
}
