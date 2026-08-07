package com.carrental.lld.reservation;

import com.carrental.lld.billing.Bill;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Standard tiered cancellation policy.
 * <p>
 * Logic: &gt;48h before start → free; &gt;24h → 20% of bill; otherwise 50%.
 */
public class StandardCancellationPolicy implements CancellationPolicy {

    @Override
    public double computeCancellationFee(VehicleReservation reservation, Bill bill,
                                         LocalDateTime cancelledAt) {
        if (bill == null) {
            return 0.0;
        }
        double total = bill.getTotal();
        long hoursUntilStart = Duration.between(cancelledAt, reservation.getStart()).toHours();

        if (hoursUntilStart > 48) {
            return 0.0;
        }
        if (hoursUntilStart > 24) {
            return total * 0.20;
        }
        return total * 0.50;
    }
}
