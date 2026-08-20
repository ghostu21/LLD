package com.lld.patterns.observer.demo;

import com.lld.patterns.observer.stock.EmailNotificationObserver;
import com.lld.patterns.observer.stock.IphoneProductObservable;
import com.lld.patterns.observer.stock.PushNotificationObserver;
import com.lld.patterns.observer.stock.StockAvailabilityObservable;
import com.lld.patterns.observer.stock.StockNotificationObserver;
import com.lld.patterns.observer.weather.CurrentConditionsDisplay;
import com.lld.patterns.observer.weather.ForecastDisplay;
import com.lld.patterns.observer.weather.WeatherObservable;
import com.lld.patterns.observer.weather.WeatherStation;

public class ObserverPatternDemo {
    public static void main(String[] args) {
        runWeatherDemo();
        runStockNotifyMeDemo();
    }

    private static void runWeatherDemo() {
        System.out.println("###### Observer Design Pattern ######");
        System.out.println("###### Example: Weather Station ######");

        WeatherObservable weatherStation = new WeatherStation();
        new CurrentConditionsDisplay(weatherStation);
        ForecastDisplay forecastDisplay = new ForecastDisplay(weatherStation);

        System.out.println("===>>> Initial Weather Update");
        weatherStation.setWeatherReadings(80, 65, 30.4f);

        System.out.println("===>>> Second Weather Update");
        weatherStation.setWeatherReadings(82, 70, 29.2f);

        weatherStation.removeObserver(forecastDisplay);

        System.out.println("===>>> Third Weather Update");
        weatherStation.setWeatherReadings(70, 21, 29.2f);
        System.out.println();
    }

    private static void runStockNotifyMeDemo() {
        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("###### E-commerce Store - Stock Availability Notification Feature Demo ######");

        StockAvailabilityObservable iphoneProduct = new IphoneProductObservable("ip15", "iphone 15", 1250, 10);

        StockNotificationObserver johnPush = new PushNotificationObserver("John123", "JohnDeviceP1");
        StockNotificationObserver katyPush = new PushNotificationObserver("Katy678", "KatyDeviceP2");
        StockNotificationObserver janeEmail = new EmailNotificationObserver("Jane783", "jane783@gmail.com");
        StockNotificationObserver georgeEmail = new EmailNotificationObserver("George993", "george993@gmail.com");

        iphoneProduct.purchase(10);

        boolean success = iphoneProduct.purchase(1);
        if (!success) {
            iphoneProduct.addStockObserver(johnPush);
            iphoneProduct.addStockObserver(katyPush);
            iphoneProduct.addStockObserver(janeEmail);
            iphoneProduct.addStockObserver(georgeEmail);
        }

        iphoneProduct.restock(20);

        iphoneProduct.purchase(1);
        iphoneProduct.purchase(1);

        iphoneProduct.removeStockObserver(johnPush);
        iphoneProduct.removeStockObserver(katyPush);

        iphoneProduct.purchase(18);
        iphoneProduct.restock(5);

        iphoneProduct.purchase(1);
        iphoneProduct.purchase(1);

        iphoneProduct.removeStockObserver(janeEmail);
        iphoneProduct.removeStockObserver(georgeEmail);
    }
}
