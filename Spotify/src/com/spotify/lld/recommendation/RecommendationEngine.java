package com.spotify.lld.recommendation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Event-driven affinity scorer (not a stub {@code getRecommendations()}).
 * <p>
 * Why: recommendations need a listen-event stream + ranking. Cold start and
 * collaborative filtering are natural extensions.
 * <p>
 * Logic: {@link #onEvent} maps {@link ListenEventType} to a score delta
 * (SKIP is more negative for short listens); scores accumulate per
 * trackId→userId. {@link #recommend} returns top-N track ids by that user's score.
 */
public class RecommendationEngine {
    /**
     * trackId → (userId → cumulative affinity).
     * Nested concurrent maps allow concurrent event ingestion.
     */
    private final Map<String, Map<String, Double>> affinityScores = new ConcurrentHashMap<>();

    /**
     * Ingests one listen interaction and updates affinity.
     * Logic: choose delta by event type → merge into scores with Double::sum.
     */
    public void onEvent(ListenEvent event) {
        double delta = switch (event.getType()) {
            case PLAY -> 1.0;
            case LIKE -> 3.0;
            case REPEAT -> 4.0;
            case ADD_TO_PLAYLIST -> 3.5;
            case SHARE -> 5.0;
            case SKIP -> event.getListenDurationMs() < 10_000 ? -2.0 : -0.5;
        };

        affinityScores
                .computeIfAbsent(event.getTrackId(), k -> new ConcurrentHashMap<>())
                .merge(event.getUserId(), delta, Double::sum);
    }

    /**
     * Ranks tracks the user has affinity for, highest score first.
     * Logic: filter tracks with a score for userId → sort desc → limit → ids.
     */
    public List<String> recommend(String userId, int limit) {
        return affinityScores.entrySet().stream()
                .filter(e -> e.getValue().containsKey(userId))
                .sorted((a, b) -> Double.compare(
                        b.getValue().getOrDefault(userId, 0.0),
                        a.getValue().getOrDefault(userId, 0.0)))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Cold-start stub: new users have no events yet.
     * Production: seed from genre popularity / global charts.
     */
    public List<String> recommendForNewUser(String genre, int limit) {
        return List.of();
    }
}
