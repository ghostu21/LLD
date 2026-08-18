package com.reco.lld.demo;

import com.reco.lld.profile.InteractionType;
import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrent recommend, tag updates, and hides on the same user.
 * <p>
 * Single-flight cache coalesces identical misses; striped locks serialize
 * writes; generation keys stop a late compute from serving as the new slate.
 */
public class ConcurrentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("-- concurrent --");
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Callable<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            tasks.add(() -> fx.reco.recommend(RecommendationRequest.builder()
                    .actor(fx.charlie)
                    .placement(Placement.HOME)
                    .limit(5)
                    .build()).getItems().size());
        }

        fx.preferences.replaceSelectedTags(fx.dana, Set.of("fitness"));
        AtomicInteger tagSlates = new AtomicInteger();
        for (int i = 0; i < 8; i++) {
            tasks.add(() -> {
                RecommendationResponse r = fx.reco.recommend(RecommendationRequest.builder()
                        .actor(fx.dana)
                        .placement(Placement.HOME)
                        .limit(5)
                        .build());
                tagSlates.incrementAndGet();
                return r.getItems().size();
            });
        }
        tasks.add(() -> {
            fx.preferences.replaceSelectedTags(fx.dana, Set.of("software", "craft"));
            return 0;
        });
        tasks.add(() -> {
            fx.interactions.record(fx.dana, fx.tshirt.getItemId(), InteractionType.HIDE);
            return 0;
        });

        int total = 0;
        for (Future<Integer> f : pool.invokeAll(tasks)) {
            total += f.get();
        }
        pool.shutdownNow();

        RecommendationResponse finalDana = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.dana)
                .placement(Placement.HOME)
                .limit(8)
                .build());
        boolean hiddenGone = finalDana.getItems().stream()
                .noneMatch(i -> i.getItemId().equals(fx.tshirt.getItemId()));
        System.out.println("Parallel HOME sizes summed=" + total
                + " danaReads=" + tagSlates.get()
                + " tshirt hidden after concurrent hide? " + hiddenGone);
        System.out.println("Dana final tags " + fx.preferences.selectedTags(fx.dana.getUserId())
                + " slate=" + finalDana.getItems());
    }
}
