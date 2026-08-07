package com.carrental.lld.addon;

/**
 * Immutable snapshot of an add-on attached to a reservation at booking time.
 * <p>
 * Why: catalog prices may change later; the reservation must bill the price
 * and quantity captured when the member booked.
 */
public class ReservationAddon {
    private final String code;
    private final String name;
    private final AddonCategory category;
    private final double unitPrice;
    private final int quantity;
    private final boolean perReservation;

    /**
     * @param addon    source catalog item
     * @param quantity number of units
     */
    public ReservationAddon(BillableAddon addon, int quantity) {
        this.code = addon.getCode();
        this.name = addon.getName();
        this.category = addon.getCategory();
        this.unitPrice = addon.getPrice();
        this.quantity = quantity;
        this.perReservation = addon.isPerReservation();
    }

    /**
     * Full constructor for rehydration from persistence.
     */
    public ReservationAddon(String code, String name, AddonCategory category,
                            double unitPrice, int quantity, boolean perReservation) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.perReservation = perReservation;
    }

    /**
     * Computes charge for this add-on line.
     * <p>
     * Logic: per-reservation → price × qty; otherwise price × qty × rental days.
     *
     * @param rentalDays number of billed rental days
     * @return line total
     */
    public double calculateCharge(int rentalDays) {
        if (perReservation) {
            return unitPrice * quantity;
        }
        return unitPrice * quantity * rentalDays;
    }

    /** @return catalog code */
    public String getCode() { return code; }

    /** @return display name */
    public String getName() { return name; }

    /** @return add-on category */
    public AddonCategory getCategory() { return category; }

    /** @return snapshotted unit price */
    public double getUnitPrice() { return unitPrice; }

    /** @return quantity */
    public int getQuantity() { return quantity; }

    /** @return true if flat per reservation */
    public boolean isPerReservation() { return perReservation; }
}
