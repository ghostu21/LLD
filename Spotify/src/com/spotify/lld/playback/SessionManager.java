package com.spotify.lld.playback;

import com.spotify.lld.auth.User;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of active playback sessions (one player per session, not global).
 * <p>
 * Why: a platform-wide MusicPlayer singleton is the wrong abstraction —
 * User A's pause must not stop User B. Sessions isolate player state per
 * user/device login.
 * <p>
 * Logic: create allocates a UUID session + {@link PlaybackSession}; get looks
 * up by id; end removes the session from the concurrent map.
 */
public class SessionManager {
    /** sessionId → live PlaybackSession (thread-safe map). */
    private final Map<String, PlaybackSession> sessions = new ConcurrentHashMap<>();

    /**
     * Opens a new session for {@code user} with its own MusicPlayer/PlayerState.
     * Logic: generate id → construct session → store → return to caller.
     */
    public PlaybackSession createSession(User user) {
        String id = UUID.randomUUID().toString();
        PlaybackSession session = new PlaybackSession(id, user);
        sessions.put(id, session);
        return session;
    }

    /** Lookup helper; empty if the session was ended or never existed. */
    public Optional<PlaybackSession> getSession(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * Tears down a session (e.g. logout / device disconnect).
     * Does not call player.shutdown — caller may do that if needed.
     */
    public void endSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
