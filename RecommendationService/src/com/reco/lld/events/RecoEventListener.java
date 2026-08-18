package com.reco.lld.events;

@FunctionalInterface
public interface RecoEventListener {
    void onEvent(RecoEvent event);
}
