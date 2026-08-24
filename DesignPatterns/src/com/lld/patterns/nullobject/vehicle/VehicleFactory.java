package com.lld.patterns.nullobject.vehicle;

/**
 * Unknown types return {@link NullVehicle} instead of {@code null}.
 */
public class VehicleFactory {
    public static Vehicle getVehicle(String type) {
        if ("car".equals(type)) {
            return new Car("Toyota", "Red", 5, 60, true);
        }
        if ("bike".equals(type)) {
            return new Bike("Yamaha", "Black", 60);
        }
        return new NullVehicle();
    }
}
