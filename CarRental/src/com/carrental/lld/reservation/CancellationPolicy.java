package com.carrental.lld.reservation;

import com.carrental.lld.billing.Bill;

import java.time.LocalDateTime;

/**
 * Strategy for computing cancellation fees based on notice period.
 */
public interface CancellationPolicy {
    /**
     * Computes cancellation fee as a fraction of the bill total.
     *
     * @param reservation reservation being cancelled
     * @param bill        current bill (may be null before generation)
     * @param cancelledAt when cancellation occurs
     * @return fee amount (not percentage)
     */
    double computeCancellationFee(VehicleReservation reservation, Bill bill, LocalDateTime cancelledAt);
}
