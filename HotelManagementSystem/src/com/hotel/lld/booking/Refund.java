package com.hotel.lld.booking;

/**
 * Result of applying a cancellation policy.
 * <p>
 * Interview takeaway: refunds are business rules, not controller logic.
 */
public final class Refund {
    private final double amount;
    private final boolean full;
    private final String reason;

    private Refund(double amount, boolean full, String reason) {
        this.amount = amount;
        this.full = full;
        this.reason = reason;
    }

    public static Refund full(double billTotal) {
        return new Refund(billTotal, true, "Full refund (>= 24h before check-in)");
    }

    public static Refund none(String reason) {
        return new Refund(0.0, false, reason);
    }

    public static Refund partial(double amount, String reason) {
        return new Refund(amount, false, reason);
    }

    public double getAmount() {
        return amount;
    }

    public boolean isFull() {
        return full;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "Refund{amount=" + amount + ", full=" + full + ", reason='" + reason + "'}";
    }
}
