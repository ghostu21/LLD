package com.lld.patterns.nullobject.demo;

import com.lld.patterns.nullobject.vehicle.Bike;
import com.lld.patterns.nullobject.vehicle.Car;
import com.lld.patterns.nullobject.vehicle.Vehicle;
import com.lld.patterns.nullobject.vehicle.VehicleFactory;

public class NullObjectPatternDemo {
    public static void main(String[] args) {
        System.out.println("\n##### Null Object Pattern: Solution Demo #####");

        Vehicle car = VehicleFactory.getVehicle("car");
        printVehicleDetails(car);
        testDrive(car);

        Vehicle bike = VehicleFactory.getVehicle("bike");
        printVehicleDetails(bike);
        testDrive(bike);

        Vehicle nullVehicle = VehicleFactory.getVehicle("null");
        printVehicleDetails(nullVehicle);
        testDrive(nullVehicle);
    }

    /**
     * No {@code vehicle != null} check. Unknown types are {@code NullVehicle},
     * which is neither Car nor Bike, so details are skipped; {@link #testDrive} still runs.
     */
    private static void printVehicleDetails(Vehicle vehicle) {
        if (vehicle instanceof Car) {
            Car car = (Car) vehicle;
            System.out.print("\n[+] Vehicle Details: ");
            System.out.println(car.getClass().getSimpleName() + " [Model=" + car.getModel()
                    + ", Color=" + car.getColor() + ", Seating Capacity=" + car.getSeatingCapacity()
                    + ", Fuel Tank Capacity=" + car.getFuelTankCapacity() + "]");
        }
        if (vehicle instanceof Bike) {
            Bike bike = (Bike) vehicle;
            System.out.print("\n[+] Vehicle Details: ");
            System.out.println(bike.getClass().getSimpleName() + " [Model=" + bike.getModel()
                    + ", Color=" + bike.getColor() + ", Fuel Tank Capacity=" + bike.getFuelTankCapacity() + "]");
        }
    }

    private static void testDrive(Vehicle vehicle) {
        vehicle.start();
        vehicle.stop();
    }
}
