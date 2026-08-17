package com.hotel.lld.booking;

import java.time.LocalDateTime;

/**
 * Strategy for computing refunds on cancellation.
 */
public interface CancellationPolicy {
    /**
     * @param booking    booking being cancelled
     * @param cancelAt   when cancellation is requested
     * @return refund decision (amount + reason)
     */
    Refund calculateRefund(RoomBooking booking, LocalDateTime cancelAt);
}
