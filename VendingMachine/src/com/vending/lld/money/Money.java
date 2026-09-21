package com.vending.lld.money;

/**
 * Money in integer cents. Never {@code float} — the screenshot's race on a
 * single {@code float balance} is also a rounding bug.
 */
public final class Money {
    private final int cents;

    private Money(int cents) {
        this.cents = cents;
    }

    public static Money zero() {
        return new Money(0);
    }

    public static Money ofCents(int cents) {
        return new Money(cents);
    }

    public static Money ofDollars(int dollars, int extraCents) {
        return new Money(dollars * 100 + extraCents);
    }

    public int cents() {
        return cents;
    }

    public Money plus(Money other) {
        return new Money(cents + other.cents);
    }

    public Money minus(Money other) {
        return new Money(cents - other.cents);
    }

    public boolean ge(Money other) {
        return cents >= other.cents;
    }

    public Money percentOff(int percent) {
        int discount = cents * percent / 100;
        return new Money(cents - discount);
    }

    @Override
    public String toString() {
        return String.format("$%.2f", cents / 100.0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money && ((Money) o).cents == cents;
    }

    @Override
    public int hashCode() {
        return cents;
    }
}
