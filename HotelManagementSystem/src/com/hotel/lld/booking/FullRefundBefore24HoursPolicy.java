package com.hotel.lld.booking;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Full refund if cancelled at least 24 hours before check-in date; otherwise none.
 */
public class FullRefundBefore24HoursPolicy implements CancellationPolicy {

    @Override
    public Refund calculateRefund(RoomBooking booking, LocalDateTime cancelAt) {
        LocalDateTime checkInStart = booking.getCheckIn().atStartOfDay();
        double total = booking.getBill() == null ? 0.0 : booking.getBill().getTotal();
        long hoursUntilCheckIn = Duration.between(cancelAt, checkInStart).toHours();

        if (hoursUntilCheckIn >= 24) {
            return Refund.full(total);
        }
        return Refund.none("Cancelled within 24h of check-in — no refund");
    }
}
