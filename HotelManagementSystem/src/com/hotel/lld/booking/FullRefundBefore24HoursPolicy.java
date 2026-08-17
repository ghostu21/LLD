package com.hotel.lld.booking;

import java.time.LocalDateTime;

/**
 * Full refund if cancelled strictly before check-in minus 24 hours; otherwise none.
 */
public class FullRefundBefore24HoursPolicy implements CancellationPolicy {

    @Override
    public Refund calculateRefund(RoomBooking booking, LocalDateTime cancelAt) {
        LocalDateTime deadline = booking.getCheckIn().atStartOfDay().minusHours(24);
        double total = booking.getBill() == null ? 0.0 : booking.getBill().getTotal();

        if (cancelAt.isBefore(deadline)) {
            return Refund.full(total);
        }
        return Refund.none("Cancelled within 24h of check-in — no refund");
    }
}
