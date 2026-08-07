package com.carrental.lld.addon;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lookup catalog for equipment add-ons.
 */
public class EquipmentCatalog {
    private final Map<String, Equipment> items = new ConcurrentHashMap<>();

    /** Registers an equipment item. */
    public void add(Equipment equipment) {
        items.put(equipment.getCode(), equipment);
    }

    /**
     * @param code catalog code
     * @return equipment or null
     */
    public Equipment findByCode(String code) {
        return items.get(code);
    }
}
