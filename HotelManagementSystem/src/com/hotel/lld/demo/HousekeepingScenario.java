package com.hotel.lld.demo;

import com.hotel.lld.booking.RoomBooking;
import com.hotel.lld.service.HouseKeepingTask;
import com.hotel.lld.service.TaskStatus;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates housekeeping workflow: CHECKOUT → BEING_SERVICED → AVAILABLE.
 */
public class HousekeepingScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Housekeeping workflow ---");
        LocalDate checkIn = LocalDate.now().plusDays(2);
        RoomBooking booking = fx.bookingService.book(
                fx.alice.getGuestId(), fx.deluxe201.getRoomNumber(), checkIn, 1);

        // Force check-in even if calendar date is in the future (demo)
        fx.bookingService.checkIn(booking.getReservationNumber());
        System.out.println("Checked in → room status="
                + fx.inventory.findByNumber("201").getStatus());

        fx.bookingService.checkOut(booking.getReservationNumber());
        System.out.println("Checked out → room status="
                + fx.inventory.findByNumber("201").getStatus());

        HouseKeepingTask task = fx.housekeeping.tasksForRoom("201").stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No pending housekeeping task for room 201"));
        System.out.println("Task " + task.getTaskId() + " status=" + task.getStatus()
                + " staff=" + task.getStaffId());

        fx.housekeeping.start(task.getTaskId());
        fx.housekeeping.complete(task.getTaskId());
        System.out.println("After complete → task=" + TaskStatus.COMPLETED
                + " room=" + fx.inventory.findByNumber("201").getStatus());

        TimeUnit.MILLISECONDS.sleep(200);
    }
}
