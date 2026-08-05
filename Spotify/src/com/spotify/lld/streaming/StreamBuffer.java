package com.spotify.lld.streaming;

public interface StreamBuffer {
    byte[] nextChunk();
    boolean hasMore();
    void reset();
}
