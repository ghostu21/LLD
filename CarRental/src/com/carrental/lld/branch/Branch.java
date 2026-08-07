package com.carrental.lld.branch;

/**
 * Physical rental branch location.
 * <p>
 * Why: multi-branch inventory, pickup, and one-way returns require branch
 * identity separate from vehicles and reservations.
 */
public class Branch {
    private final String branchId;
    private final String name;
    private final String city;
    private final String address;

    /**
     * @param branchId unique branch identifier
     * @param name     branch display name
     * @param city     city where branch operates
     * @param address  street address
     */
    public Branch(String branchId, String name, String city, String address) {
        this.branchId = branchId;
        this.name = name;
        this.city = city;
        this.address = address;
    }

    /** @return unique branch id */
    public String getBranchId() { return branchId; }

    /** @return branch name */
    public String getName() { return name; }

    /** @return city */
    public String getCity() { return city; }

    /** @return street address */
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return name + " (" + city + ")";
    }
}
