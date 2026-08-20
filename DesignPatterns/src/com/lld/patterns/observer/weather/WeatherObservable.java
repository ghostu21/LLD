package com.lld.patterns.observer.weather;

/**
 * Subject: add / remove / notify observers, plus the state writers the station uses.
 */
public interface WeatherObservable {
    void addObserver(WeatherObserver observer);

    void removeObserver(WeatherObserver observer);

    /// uses Push Mechanism for Data
    void notifyObservers();

    void setWeatherReadings(float temperature, float humidity, float pressure);
}
