package com.hotel.lld.service;

/**
 * Catalog amenity guests can request (extra pillow, late checkout, etc.).
 */
public class Amenity {
    private final String amenityId;
    private final String name;
    private final double price;

    public Amenity(String amenityId, String name, double price) {
        this.amenityId = amenityId;
        this.name = name;
        this.price = price;
    }

    public String getAmenityId() {
        return amenityId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public ServiceCharge toCharge(String chargeId) {
        return new ServiceCharge(chargeId, ChargeType.AMENITY, name, price);
    }
}
