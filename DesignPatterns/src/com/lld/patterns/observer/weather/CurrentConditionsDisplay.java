package com.lld.patterns.observer.weather;

public class CurrentConditionsDisplay implements WeatherObserver {
    private final WeatherObservable weatherStation;

    public CurrentConditionsDisplay(WeatherObservable weatherStation) {
        this.weatherStation = weatherStation;
        weatherStation.addObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Saving weather data... ");
        display();
    }

    public void display() {
        System.out.println("Current Weather Conditions: " + weatherStation.toString());
    }
}
