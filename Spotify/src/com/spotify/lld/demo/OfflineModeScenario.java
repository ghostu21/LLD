package com.spotify.lld.demo;

import com.spotify.lld.offline.DownloadManager;

public class OfflineModeScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Offline Mode (DRM-style constraints) ---");
        DownloadManager downloads = new DownloadManager();
        String device = "device-alice-phone";

        downloads.download(fx.alice.getUserId(), fx.song1, device, "drm-token-abc");

        System.out.println("Playable on registered device: "
                + downloads.getPlayableTrack(fx.alice.getUserId(), fx.song1.getId(), device).isPresent());
        System.out.println("Playable on wrong device: "
                + downloads.getPlayableTrack(fx.alice.getUserId(), fx.song1.getId(), "other-device").isPresent());
    }
}
