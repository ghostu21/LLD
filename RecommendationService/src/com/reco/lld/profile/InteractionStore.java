package com.reco.lld.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Append-only interaction log keyed by user (stand-in for an event store).
 * <p>
 * Why: collaborative filtering needs <em>other users'</em> purchase sets, but
 * those sets never leave this store into the HTTP response — only item scores do.
 */
public class InteractionStore {
    private final ConcurrentHashMap<String, List<Interaction>> byUser = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Interaction> all = new CopyOnWriteArrayList<>();

    public void append(Interaction interaction) {
        byUser.computeIfAbsent(interaction.getUserId(), k -> new CopyOnWriteArrayList<>())
                .add(interaction);
        all.add(interaction);
    }

    public List<Interaction> forUser(String userId) {
        return Collections.unmodifiableList(byUser.getOrDefault(userId, List.of()));
    }

    public List<Interaction> all() {
        return Collections.unmodifiableList(all);
    }

    public List<String> userIds() {
        return new ArrayList<>(byUser.keySet());
    }
}
