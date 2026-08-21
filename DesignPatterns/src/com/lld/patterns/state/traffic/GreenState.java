package com.lld.patterns.state.traffic;

public class GreenState implements TrafficLightState {
    @Override
    public void action(TrafficLight signal) {
        System.out.println("GREEN: Go → next YELLOW");
        signal.setState(new YellowState());
    }
}
