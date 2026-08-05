package com.spotify.lld.social;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Directed follow graph: User → follows → User.
 * <p>
 * Why: social features need explicit edges. Follow is not symmetric — Alice
 * following Bob does not imply Bob follows Alice (needed for FOLLOWERS_ONLY ACL).
 * <p>
 * Logic: maintain both adjacency maps (following + followers) so queries are
 * O(1) set lookups. Self-follow is rejected.
 */
public class SocialGraph {
    /** followerId → set of userIds they follow. */
    private final Map<String, Set<String>> following = new ConcurrentHashMap<>();
    /** targetId → set of userIds who follow them. */
    private final Map<String, Set<String>> followers = new ConcurrentHashMap<>();

    /**
     * Creates a directed edge follower → target and updates both indexes.
     */
    public void follow(String followerId, String targetId) {
        if (followerId.equals(targetId)) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }
        following.computeIfAbsent(followerId, k -> ConcurrentHashMap.newKeySet()).add(targetId);
        followers.computeIfAbsent(targetId, k -> ConcurrentHashMap.newKeySet()).add(followerId);
    }

    /** Removes the directed edge if present (idempotent). */
    public void unfollow(String followerId, String targetId) {
        following.getOrDefault(followerId, Set.of()).remove(targetId);
        followers.getOrDefault(targetId, Set.of()).remove(followerId);
    }

    /** @return true if followerId currently follows targetId. */
    public boolean isFollowing(String followerId, String targetId) {
        return following.getOrDefault(followerId, Set.of()).contains(targetId);
    }

    /** Users who follow {@code userId} (inbound edges). */
    public Set<String> getFollowers(String userId) {
        return Collections.unmodifiableSet(followers.getOrDefault(userId, Set.of()));
    }

    /** Users that {@code userId} follows (outbound edges). */
    public Set<String> getFollowing(String userId) {
        return Collections.unmodifiableSet(following.getOrDefault(userId, Set.of()));
    }
}
