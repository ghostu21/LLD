package com.lld.patterns.state.traffic;

public class TrafficLight {
    private TrafficLightState state;

    public TrafficLight() {
        this.state = new RedState();
    }

    public void setState(TrafficLightState state) {
        this.state = state;
    }

    public TrafficLightState getState() {
        return state;
    }

    public void change() {
        state.action(this);
    }
}
