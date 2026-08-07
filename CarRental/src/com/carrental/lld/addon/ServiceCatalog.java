package com.carrental.lld.addon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup catalog for service add-ons.
 */
public class ServiceCatalog {
    private final Map<String, ServiceAddon> items = new ConcurrentHashMap<>();

    /** Registers a service add-on. */
    public void add(ServiceAddon service) {
        items.put(service.getCode(), service);
    }

    /**
     * @param code catalog code
     * @return service or null
     */
    public ServiceAddon findByCode(String code) {
        return items.get(code);
    }
}
