package com.lld.patterns.strategy.demo;

import com.lld.patterns.strategy.payment.CreditCardPayment;
import com.lld.patterns.strategy.payment.PayPalPayment;
import com.lld.patterns.strategy.payment.ShoppingCart;
import com.lld.patterns.strategy.payment.UPIPayment;
import com.lld.patterns.strategy.vehicle.EVDrive;
import com.lld.patterns.strategy.vehicle.GoodsVehicle;
import com.lld.patterns.strategy.vehicle.HybridVehicle;
import com.lld.patterns.strategy.vehicle.NormalDrive;
import com.lld.patterns.strategy.vehicle.OffRoadVehicle;
import com.lld.patterns.strategy.vehicle.SportsDrive;
import com.lld.patterns.strategy.vehicle.SportsVehicle;
import com.lld.patterns.strategy.vehicle.Vehicle;

/**
 * Runnable demos matching the Strategy Pattern examples:
 * vehicle drive modes (constructor injection) and cart payments (setter injection).
 */
public class StrategyPatternDemo {
    public static void main(String[] args) {
        runVehicleDemo();
        runPaymentDemo();
    }

    private static void runVehicleDemo() {
        System.out.println("###### Strategy Design Pattern ######");
        System.out.println("###### Example: Vehicle Drive Modes ######");

        Vehicle vehicle = new SportsVehicle(new SportsDrive());
        vehicle.drive();

        vehicle = new GoodsVehicle(new NormalDrive());
        vehicle.drive();

        vehicle = new HybridVehicle(new EVDrive());
        vehicle.drive();

        vehicle = new OffRoadVehicle(new SportsDrive());
        vehicle.drive();

        // Same vehicle, different algorithm at runtime (eco → sports).
        vehicle.setDriveStrategy(new NormalDrive());
        vehicle.drive();
        System.out.println();
    }

    private static void runPaymentDemo() {
        System.out.println("###### Strategy Design Pattern ######");
        System.out.println("###### Example: Payment Processor ######");

        ShoppingCart cart = new ShoppingCart();
        cart.setPaymentStrategy(new CreditCardPayment("1234-5678-9012-3456"));
        cart.checkout(100.0);
        cart.setPaymentStrategy(new PayPalPayment("johndoe@example.com"));
        cart.checkout(200.0);
        cart.setPaymentStrategy(new UPIPayment("9988776655@ybl"));
        cart.checkout(300.0);
        // CryptoPayment would be a new class — ShoppingCart does not change.
    }
}
