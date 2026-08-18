package com.reco.lld.ranking;

import java.util.List;

/**
 * Strategy interface for producing a scored candidate list.
 * <p>
 * Why: homepage vs PDP vs cold-start need different algorithms without
 * a giant switch inside the facade.
 */
public interface RankingStrategy {
    String name();

    List<ScoredItem> rank(RankingContext context);
}
