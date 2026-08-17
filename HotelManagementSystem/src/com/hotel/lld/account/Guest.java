package com.hotel.lld.account;

/**
 * Hotel guest who searches, books, and receives notifications.
 */
public class Guest {
    private final String guestId;
    private final String name;
    private final String email;
    private final String phone;
    private AccountStatus status;
    private int totalRoomsCheckedIn;

    public Guest(String guestId, String name, String email, String phone) {
        this.guestId = guestId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.status = AccountStatus.ACTIVE;
        this.totalRoomsCheckedIn = 0;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public int getTotalRoomsCheckedIn() {
        return totalRoomsCheckedIn;
    }

    public void incrementCheckedIn() {
        totalRoomsCheckedIn++;
    }
}
