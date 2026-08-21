package com.lld.patterns.state.traffic;

public class RedState implements TrafficLightState {
    @Override
    public void action(TrafficLight signal) {
        System.out.println("RED: Stop → next GREEN");
        signal.setState(new GreenState());
    }
}
