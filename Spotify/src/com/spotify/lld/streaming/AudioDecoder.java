package com.spotify.lld.streaming;

/**
 * Strategy for turning a raw audio chunk into playable frames.
 * <p>
 * Why: separates transport buffering from codec work so StreamingPlayer can
 * stay generic (AAC/Opus/etc. would plug in here in production).
 * <p>
 * Logic: implementors receive successive chunks from {@link StreamBuffer};
 * demos often use a lambda that logs chunk sizes.
 */
@FunctionalInterface
public interface AudioDecoder {
    /**
     * Decode/render one chunk.
     * @param chunk next slice of the stream (may be shorter than full chunk size at EOF)
     */
    void decode(byte[] chunk);
}
