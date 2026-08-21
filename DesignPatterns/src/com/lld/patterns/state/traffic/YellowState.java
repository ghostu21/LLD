package com.lld.patterns.state.traffic;

public class YellowState implements TrafficLightState {
    @Override
    public void action(TrafficLight signal) {
        System.out.println("YELLOW: Slow down → next RED");
        signal.setState(new RedState());
    }
}
