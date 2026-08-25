package com.lld.patterns.abstractfactory.demo;

import com.lld.patterns.abstractfactory.car.CarFactory;
import com.lld.patterns.abstractfactory.car.CarFactoryProvider;
import com.lld.patterns.abstractfactory.car.CarType;

public class AbstractFactoryPatternDemo {
    public static void main(String[] args) {
        System.out.println("=====Abstract Factory Design Pattern=====");
        CarFactoryProvider carFactoryProvider = new CarFactoryProvider();

        CarFactory economyCar = carFactoryProvider.getFactory(CarType.ECONOMY, "Honda");
        economyCar.produceCompleteVehicle();

        CarFactory luxuryCar = carFactoryProvider.getFactory(CarType.LUXURY, "Mercedes");
        luxuryCar.produceCompleteVehicle();

        CarFactory premiumCar = carFactoryProvider.getFactory(CarType.PREMIUM, "Rolls Royce");
        premiumCar.produceCompleteVehicle();
    }
}
