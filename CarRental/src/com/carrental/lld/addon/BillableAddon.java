package com.carrental.lld.addon;

/**
 * Contract for any billable rental add-on (equipment, service, insurance).
 * <p>
 * Why: unified billing iterates {@link BillableAddon} implementations without
 * separate code paths per category.
 */
public interface BillableAddon {
    /** @return catalog code */
    String getCode();

    /** @return display name */
    String getName();

    /** @return category for bill line mapping */
    AddonCategory getCategory();

    /** @return unit price */
    double getPrice();

    /**
     * Whether charge is flat per reservation vs per rental day.
     *
     * @return true if priced once per reservation
     */
    boolean isPerReservation();
}
