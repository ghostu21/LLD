package com.spotify.lld.events;

public class NotificationService implements MusicEventListener {
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
