package com.spotify.lld.streaming;

import java.util.Arrays;

/**
 * In-memory chunked view over a track's byte payload.
 * <p>
 * Why: streaming delivers audio in fixed-size chunks, not as one giant
 * {@code play()} call. This buffer is the producer side of the pipeline.
 * <p>
 * Logic: keep an {@code offset} into {@code data}; {@link #nextChunk} returns
 * the next up-to-{@code CHUNK_SIZE} bytes and advances offset;
 * {@link #hasMore} is offset &lt; length; {@link #reset} rewinds for replay/seek demos.
 */
public class AudioStreamBuffer implements StreamBuffer {
    private final byte[] data;
    /** Current read position into {@code data} (visibility for hasMore checks). */
    private volatile int offset = 0;
    private static final int CHUNK_SIZE = 4096;

    /** Wraps the full track bytes (demo: usually Song.getData()). */
    public AudioStreamBuffer(byte[] data) {
        this.data = data;
    }

    /**
     * Returns the next chunk and advances the read cursor.
     * Logic: if exhausted return empty; else copy [offset, min(offset+CHUNK, end)).
     * Synchronized so concurrent nextChunk/reset do not tear the offset.
     */
    @Override
    public synchronized byte[] nextChunk() {
        if (!hasMore()) return new byte[0];
        int end = Math.min(offset + CHUNK_SIZE, data.length);
        byte[] chunk = Arrays.copyOfRange(data, offset, end);
        offset = end;
        return chunk;
    }

    /** @return true while unread bytes remain. */
    @Override
    public boolean hasMore() {
        return offset < data.length;
    }

    /** Rewinds to the start of the track (seek-to-zero demo). */
    @Override
    public synchronized void reset() {
        offset = 0;
    }
}
