package com.hotel.lld.booking;

import com.hotel.lld.billing.Bill;
import com.hotel.lld.service.ServiceCharge;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reservation of one room for a guest over a date window.
 */
public class RoomBooking {
    private final String reservationNumber;
    private final String guestId;
    private final String roomNumber;
    private final LocalDate checkIn;
    private final int durationInDays;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private BookingStatus status;
    private Bill bill;
    private final List<ServiceCharge> charges = new ArrayList<>();

    public RoomBooking(String reservationNumber, String guestId, String roomNumber,
                       LocalDate checkIn, int durationInDays) {
        this.reservationNumber = reservationNumber;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.checkIn = checkIn;
        this.durationInDays = durationInDays;
        this.status = BookingStatus.PENDING;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    /** Exclusive checkout date (check-in + nights). */
    public LocalDate getCheckOut() {
        return checkIn.plusDays(durationInDays);
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public LocalDateTime getActualCheckIn() {
        return actualCheckIn;
    }

    public void setActualCheckIn(LocalDateTime actualCheckIn) {
        this.actualCheckIn = actualCheckIn;
    }

    public LocalDateTime getActualCheckOut() {
        return actualCheckOut;
    }

    public void setActualCheckOut(LocalDateTime actualCheckOut) {
        this.actualCheckOut = actualCheckOut;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public List<ServiceCharge> getCharges() {
        return Collections.unmodifiableList(charges);
    }

    public void addCharge(ServiceCharge charge) {
        charges.add(charge);
    }
}
