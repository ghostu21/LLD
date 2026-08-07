package com.carrental.lld.addon;

/**
 * Physical equipment add-on (GPS, child seat, ski rack, etc.).
 */
public class Equipment implements BillableAddon {
    private final String code;
    private final String name;
    private final double price;
    private final boolean perReservation;

    /**
     * @param code           catalog code
     * @param name           display name
     * @param price          unit price
     * @param perReservation true if flat per reservation
     */
    public Equipment(String code, String name, double price, boolean perReservation) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.perReservation = perReservation;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getName() { return name; }

    @Override
    public AddonCategory getCategory() { return AddonCategory.EQUIPMENT; }

    @Override
    public double getPrice() { return price; }

    @Override
    public boolean isPerReservation() { return perReservation; }
}
