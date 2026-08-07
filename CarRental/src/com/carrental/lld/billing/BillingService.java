package com.carrental.lld.billing;

import com.carrental.lld.addon.AddonCategory;
import com.carrental.lld.addon.ReservationAddon;
import com.carrental.lld.reservation.VehicleReservation;
import com.carrental.lld.vehicle.Vehicle;

import java.time.temporal.ChronoUnit;

/**
 * Builds itemized bills from reservations and appends late fees.
 * <p>
 * Why: centralizes pricing rules (daily base rate, add-on snapshots, fines)
 * away from reservation state transitions.
 * <p>
 * Logic: base charge = vehicle.dailyRate × rentalDays; each
 * {@link ReservationAddon} maps to the correct {@link BillItemType}.
 */
public class BillingService {

    /**
     * Generates a new bill from reservation data and vehicle rates.
     *
     * @param reservation confirmed or active reservation
     * @param vehicle     rented vehicle (for daily rate)
     * @return itemized unpaid bill
     */
    public Bill generateBill(VehicleReservation reservation, Vehicle vehicle) {
        Bill bill = new Bill();
        int rentalDays = computeRentalDays(reservation);

        double baseCharge = vehicle.getDailyRate() * rentalDays;
        bill.addItem(new BillItem(BillItemType.BASE_CHARGE,
                "Vehicle rental (" + rentalDays + " day(s))", baseCharge));

        for (ReservationAddon addon : reservation.getAddons()) {
            double charge = addon.calculateCharge(rentalDays);
            BillItemType type = mapAddonCategory(addon.getCategory());
            bill.addItem(new BillItem(type, addon.getName() + " x" + addon.getQuantity(), charge));
        }

        reservation.setBill(bill);
        return bill;
    }

    /**
     * Appends a late-return fine line to an existing bill.
     *
     * @param bill        bill to mutate
     * @param hoursLate   overdue hours
     * @param feePerHour  vehicle late fee rate
     */
    public void appendLateFee(Bill bill, long hoursLate, double feePerHour) {
        if (hoursLate <= 0) {
            return;
        }
        double fine = hoursLate * feePerHour;
        bill.addItem(new BillItem(BillItemType.FINE,
                "Late return (" + hoursLate + " hour(s))", fine));
    }

    /**
     * Rental days = inclusive calendar span, minimum one day.
     */
    int computeRentalDays(VehicleReservation reservation) {
        long days = ChronoUnit.DAYS.between(
                reservation.getStart().toLocalDate(),
                reservation.getEnd().toLocalDate());
        return (int) Math.max(1, days == 0 ? 1 : days);
    }

    private BillItemType mapAddonCategory(AddonCategory category) {
        return switch (category) {
            case EQUIPMENT -> BillItemType.EQUIPMENT;
            case SERVICE -> BillItemType.ADDITIONAL_SERVICE;
            case INSURANCE -> BillItemType.INSURANCE;
        };
    }
}
