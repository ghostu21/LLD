package com.hotel.lld.hotel;

/**
 * Property that owns a set of rooms.
 */
public class Hotel {
    private final String hotelId;
    private final String name;
    private final String city;
    private final String address;

    public Hotel(String hotelId, String name, String city, String address) {
        this.hotelId = hotelId;
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public String getHotelId() {
        return hotelId;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }
}
