package com.spotify.lld.streaming;

public interface AudioDecoder {
    void decode(byte[] chunk);
}
