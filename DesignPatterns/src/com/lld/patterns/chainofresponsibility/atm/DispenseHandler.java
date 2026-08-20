package com.lld.patterns.chainofresponsibility.atm;

/**
 * Exclusive CoR: dispense as many notes of this denomination as possible, then
 * pass the remainder. Contrast with logging, which always forwards.
 */
public abstract class DispenseHandler {
    protected DispenseHandler next;

    public void setNext(DispenseHandler next) {
        this.next = next;
    }

    public void dispense(int amount) {
        int note = noteValue();
        if (amount >= note) {
            int count = amount / note;
            int remainder = amount % note;
            System.out.println("Dispensing " + count + " x ₹" + note);
            if (remainder > 0 && next != null) {
                next.dispense(remainder);
            } else if (remainder > 0) {
                System.out.println("Cannot dispense remaining ₹" + remainder);
            }
        } else if (next != null) {
            next.dispense(amount);
        } else {
            System.out.println("Cannot dispense ₹" + amount);
        }
    }

    protected abstract int noteValue();
}
