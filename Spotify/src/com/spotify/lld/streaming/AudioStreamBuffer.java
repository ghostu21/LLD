package com.spotify.lld.streaming;

import java.util.Arrays;

public class AudioStreamBuffer implements StreamBuffer {
    private final byte[] data;
    private volatile int offset = 0;
    private static final int CHUNK_SIZE = 4096;

    public AudioStreamBuffer(byte[] data) {
        this.data = data;
    }

    @Override
    public synchronized byte[] nextChunk() {
        if (!hasMore()) return new byte[0];
        int end = Math.min(offset + CHUNK_SIZE, data.length);
        byte[] chunk = Arrays.copyOfRange(data, offset, end);
        offset = end;
        return chunk;
    }

    @Override
    public boolean hasMore() {
        return offset < data.length;
    }

    @Override
    public synchronized void reset() {
        offset = 0;
    }
}
