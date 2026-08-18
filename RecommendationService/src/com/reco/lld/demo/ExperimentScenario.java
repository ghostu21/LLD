package com.reco.lld.demo;

import com.reco.lld.experiment.ExperimentAssigner;
import com.reco.lld.experiment.ExperimentBucket;

/**
 * Sticky A/B bucket from user id hash.
 */
public class ExperimentScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- experiment buckets --");
        ExperimentAssigner assigner = new ExperimentAssigner();
        System.out.println("Alice: " + assigner.assign(fx.alice.getUserId()));
        System.out.println("Bob:   " + assigner.assign(fx.bob.getUserId()));
        System.out.println("Same user twice is sticky: "
                + (assigner.assign(fx.alice.getUserId()) == assigner.assign(fx.alice.getUserId())));
        int control = 0;
        int treatment = 0;
        for (int i = 0; i < 200; i++) {
            ExperimentBucket b = assigner.assign("user-" + i);
            if (b == ExperimentBucket.CONTROL) control++;
            else treatment++;
        }
        System.out.println("200 synthetic users → CONTROL=" + control + " TREATMENT=" + treatment);
    }
}
