package com.spotify.lld.events;

/**
 * Example {@link MusicEventListener} that routes events to notification channels.
 * <p>
 * Why: proves Observer is wired end-to-end (subscribe → publish → fan-out).
 * <p>
 * Logic: switch on event type → push / in-app / email stubs (println in demo).
 */
public class NotificationService implements MusicEventListener {
    /**
     * Dispatches by type: NEW_RELEASE→push, FRIEND_LIKED→in-app,
     * USER_FOLLOWED→email; other types ignored in this stub.
     */
    @Override
    public void onEvent(MusicEvent event) {
        switch (event.type) {
            case NEW_RELEASE -> sendPushNotification(event.actorId, event.payload);
            case FRIEND_LIKED -> sendInAppNotification(event.actorId, event.payload);
            case USER_FOLLOWED -> sendEmail(event.actorId, "You have a new follower!");
            default -> {}
        }
    }

    private void sendPushNotification(String userId, String msg) {
        System.out.println("  [push] to " + userId + ": " + msg);
    }

    private void sendInAppNotification(String userId, String msg) {
        System.out.println("  [in-app] to " + userId + ": " + msg);
    }

    private void sendEmail(String userId, String msg) {
        System.out.println("  [email] to " + userId + ": " + msg);
    }
}
