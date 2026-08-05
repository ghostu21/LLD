package com.spotify.lld.streaming;

/**
 * Abstraction over a chunked audio source (network, memory, or file).
 * <p>
 * Why: StreamingPlayer depends on this interface, not a concrete buffer,
 * so alternate sources (HTTP range fetches, disk cache) can be swapped in.
 * <p>
 * Logic: consumers call {@link #hasMore}/{@link #nextChunk} until drained;
 * {@link #reset} supports replay from the start.
 */
public interface StreamBuffer {
    /** Pull the next chunk of bytes (empty array if none left). */
    byte[] nextChunk();

    /** Whether more chunks can be produced. */
    boolean hasMore();

    /** Rewind to the beginning of the stream. */
    void reset();
}
