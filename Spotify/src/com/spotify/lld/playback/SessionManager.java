package com.spotify.lld.playback;

import com.spotify.lld.auth.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final Map<String, PlaybackSession> sessions = new ConcurrentHashMap<>();

    public PlaybackSession createSession(User user) {
        String id = UUID.randomUUID().toString();
        PlaybackSession session = new PlaybackSession(id, user);
        sessions.put(id, session);
        return session;
    }

    public Optional<PlaybackSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void endSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
