package com.carrental.lld.addon;

/**
 * Insurance product offered at checkout (collision, liability, etc.).
 */
public class InsuranceProduct implements BillableAddon {
    private final String code;
    private final String name;
    private final double price;
    private final boolean perReservation;
    private final RentalInsurance policyDetails;

    /**
     * @param code           catalog code
     * @param name           display name
     * @param price          daily or flat price
     * @param perReservation pricing model flag
     * @param policyDetails  coverage details
     */
    public InsuranceProduct(String code, String name, double price, boolean perReservation,
                            RentalInsurance policyDetails) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.perReservation = perReservation;
        this.policyDetails = policyDetails;
    }

    @Override
    public String getCode() { return code; }

    @Override
    public String getName() { return name; }

    @Override
    public AddonCategory getCategory() { return AddonCategory.INSURANCE; }

    @Override
    public double getPrice() { return price; }

    @Override
    public boolean isPerReservation() { return perReservation; }

    /** @return linked policy metadata */
    public RentalInsurance getPolicyDetails() { return policyDetails; }
}
