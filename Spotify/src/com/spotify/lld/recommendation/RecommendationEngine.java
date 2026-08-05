package com.spotify.lld.recommendation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecommendationEngine {
    private final Map<String, Map<String, Double>> affinityScores = new ConcurrentHashMap<>();

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

    public List<String> recommendForNewUser(String genre, int limit) {
        return List.of();
    }
}
