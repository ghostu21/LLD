package com.carrental.lld.addon;

/**
 * Insurance policy metadata separate from the sellable product wrapper.
 * <p>
 * Why: policy limits and deductibles are domain data distinct from catalog price.
 */
public class RentalInsurance {
    private final String policyId;
    private final String coverageType;
    private final double deductible;
    private final double maxCoverage;

    /**
     * @param policyId     policy identifier
     * @param coverageType e.g. COLLISION, LIABILITY
     * @param deductible   member deductible amount
     * @param maxCoverage  maximum payout
     */
    public RentalInsurance(String policyId, String coverageType, double deductible, double maxCoverage) {
        this.policyId = policyId;
        this.coverageType = coverageType;
        this.deductible = deductible;
        this.maxCoverage = maxCoverage;
    }

    /** @return policy id */
    public String getPolicyId() { return policyId; }

    /** @return coverage type label */
    public String getCoverageType() { return coverageType; }

    /** @return deductible amount */
    public double getDeductible() { return deductible; }

    /** @return max coverage limit */
    public double getMaxCoverage() { return maxCoverage; }
}
