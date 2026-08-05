package com.spotify.lld.social;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SocialGraph {
    private final Map<String, Set<String>> following = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> followers = new ConcurrentHashMap<>();

    public void follow(String followerId, String targetId) {
        if (followerId.equals(targetId)) {
            throw new IllegalArgumentException("A user cannot follow themselves.");
        }
        following.computeIfAbsent(followerId, k -> ConcurrentHashMap.newKeySet()).add(targetId);
        followers.computeIfAbsent(targetId, k -> ConcurrentHashMap.newKeySet()).add(followerId);
    }

    public void unfollow(String followerId, String targetId) {
        following.getOrDefault(followerId, Set.of()).remove(targetId);
        followers.getOrDefault(targetId, Set.of()).remove(followerId);
    }

    public boolean isFollowing(String followerId, String targetId) {
        return following.getOrDefault(followerId, Set.of()).contains(targetId);
    }

    public Set<String> getFollowers(String userId) {
        return Collections.unmodifiableSet(followers.getOrDefault(userId, Set.of()));
    }

    public Set<String> getFollowing(String userId) {
        return Collections.unmodifiableSet(following.getOrDefault(userId, Set.of()));
    }
}
