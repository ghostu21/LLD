package com.amazon.lld.account;

/**
 * Immutable shipping/billing address value object.
 * <p>
 * Why: orders snapshot a delivery address at checkout; a single reusable type
 * keeps member profiles and order records consistent.
 * <p>
 * Logic: holds street, city, state, zip, country as plain strings with getters
 * only — callers build new instances when an address changes.
 */
public class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zip;
    private final String country;

    /**
     * Creates an address with all required fields.
     *
     * @param street  street line
     * @param city    city name
     * @param state   state or province
     * @param zip     postal code
     * @param country country code or name
     */
    public Address(String street, String city, String state, String zip, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }

    /** @return street line */
    public String getStreet() { return street; }

    /** @return city */
    public String getCity() { return city; }

    /** @return state or province */
    public String getState() { return state; }

    /** @return postal code */
    public String getZip() { return zip; }

    /** @return country */
    public String getCountry() { return country; }

    /**
     * Human-readable single-line summary for logs and notifications.
     */
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + zip + ", " + country;
    }
}
