package com.spotify.lld.demo;

import com.spotify.lld.events.AsyncEventBus;
import com.spotify.lld.events.MusicEvent;
import com.spotify.lld.events.NotificationService;

/**
 * Demo: async Observer — publish like/release/follow → NotificationService fan-out.
 * <p>
 * Interview angle: Observer must be async; sync listeners block the play path.
 */
public class EventBusScenario implements FeatureScenario {
    /** Subscribes NotificationService, publishes three event types, waits for workers. */
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Async Event Bus (Observer) ---");
        AsyncEventBus bus = new AsyncEventBus();
        NotificationService notifications = new NotificationService();
        bus.subscribe(MusicEvent.Type.FRIEND_LIKED, notifications);
        bus.subscribe(MusicEvent.Type.NEW_RELEASE, notifications);
        bus.subscribe(MusicEvent.Type.USER_FOLLOWED, notifications);

        bus.publish(new MusicEvent(MusicEvent.Type.FRIEND_LIKED, fx.bob.getUserId(),
                fx.bob.getUsername() + " liked " + fx.song1.getTitle()));
        bus.publish(new MusicEvent(MusicEvent.Type.NEW_RELEASE, fx.alice.getUserId(),
                "New album from Queen"));
        bus.publish(new MusicEvent(MusicEvent.Type.USER_FOLLOWED, fx.alice.getUserId(),
                "new follower"));

        Thread.sleep(300);
        bus.shutdown();
    }
}
