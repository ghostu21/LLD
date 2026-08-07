package com.carrental.lld.addon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup catalog for insurance products.
 */
public class InsuranceCatalog {
    private final Map<String, InsuranceProduct> items = new ConcurrentHashMap<>();

    /** Registers an insurance product. */
    public void add(InsuranceProduct product) {
        items.put(product.getCode(), product);
    }

    /**
     * @param code catalog code
     * @return product or null
     */
    public InsuranceProduct findByCode(String code) {
        return items.get(code);
    }
}
