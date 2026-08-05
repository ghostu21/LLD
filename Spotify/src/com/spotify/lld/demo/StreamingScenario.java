package com.spotify.lld.demo;

import com.spotify.lld.streaming.AudioStreamBuffer;
import com.spotify.lld.streaming.StreamingPlayer;

public class StreamingScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Chunked Streaming ---");
        StreamingPlayer player = new StreamingPlayer();
        AudioStreamBuffer buffer = new AudioStreamBuffer(fx.song1.getData());

        var future = player.stream(buffer, chunk ->
                System.out.println("  decoded chunk: " + chunk.length + " bytes"));
        future.get();
        player.shutdown();
        System.out.println("Stream finished for: " + fx.song1.getTitle());
    }
}
