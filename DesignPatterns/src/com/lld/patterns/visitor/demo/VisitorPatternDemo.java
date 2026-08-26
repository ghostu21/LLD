package com.lld.patterns.visitor.demo;

import com.lld.patterns.visitor.hotel.DeluxeRoom;
import com.lld.patterns.visitor.hotel.HousekeepingVisitor;
import com.lld.patterns.visitor.hotel.IRoom;
import com.lld.patterns.visitor.hotel.IRoomVisitor;
import com.lld.patterns.visitor.hotel.PricingVisitor;
import com.lld.patterns.visitor.hotel.RoomServiceVisitor;
import com.lld.patterns.visitor.hotel.StandardRoom;
import com.lld.patterns.visitor.hotel.SuiteRoom;

public class VisitorPatternDemo {
    public static void main(String[] args) {
        System.out.println("\n###### Visitor Design Pattern Demo ######");

        IRoom[] rooms = {
                new StandardRoom("101"),
                new DeluxeRoom("201", true),
                new SuiteRoom("301", 3),
                new StandardRoom("102"),
                new DeluxeRoom("202", false)
        };

        System.out.println("\n==> Housekeeping Service");
        IRoomVisitor housekeeping = new HousekeepingVisitor();
        for (IRoom room : rooms) {
            room.accept(housekeeping);
        }

        System.out.println("\n==> Room Service");
        IRoomVisitor roomService = new RoomServiceVisitor("Breakfast");
        rooms[0].accept(roomService);
        rooms[1].accept(roomService);
        rooms[2].accept(roomService);

        System.out.println("\n==> Revenue Calculation");
        PricingVisitor pricing = new PricingVisitor();
        for (IRoom room : rooms) {
            room.accept(pricing);
        }
        System.out.println("Total Revenue: Rs." + pricing.getTotalRevenue());
    }
}
