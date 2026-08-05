package com.spotify.lld.demo;

/**
 * Contract for one runnable interview/demo scenario.
 * <p>
 * Why: keeps {@link MusicStreamingService} open for new demos without editing
 * a giant switch — register a new implementation in the SCENARIOS map.
 * <p>
 * Logic: {@link #run} receives shared {@link DemoFixtures} (users/songs) and
 * prints before/after behavior for one LLD concern.
 */
@FunctionalInterface
public interface FeatureScenario {
    /**
     * Execute the scenario using shared fixtures.
     * @param fx pre-built Alice/Bob users and sample songs
     */
    void run(DemoFixtures fx) throws Exception;
}
