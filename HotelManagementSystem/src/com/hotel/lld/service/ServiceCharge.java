package com.hotel.lld.service;

import java.time.LocalDateTime;

/**
 * Billable charge attached to a stay (food, amenity, room service).
 */
public class ServiceCharge {
    private final String chargeId;
    private final ChargeType type;
    private final String description;
    private final double amount;
    private final LocalDateTime createdAt;

    public ServiceCharge(String chargeId, ChargeType type, String description, double amount) {
        this.chargeId = chargeId;
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public String getChargeId() {
        return chargeId;
    }

    public ChargeType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
