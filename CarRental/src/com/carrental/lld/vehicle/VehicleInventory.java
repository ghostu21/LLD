package com.carrental.lld.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory vehicle inventory keyed by barcode.
 * <p>
 * Why: reservations need fast lookup and branch/type filtering without a
 * database in this LLD demo.
 * <p>
 * Logic: {@link ConcurrentHashMap} stores all units; search methods filter by
 * type and branch in memory.
 */
public class VehicleInventory {
    private final Map<String, Vehicle> vehicles = new ConcurrentHashMap<>();

    /**
     * Adds or replaces a vehicle in inventory.
     *
     * @param vehicle unit to register
     */
    public void add(Vehicle vehicle) {
        vehicles.put(vehicle.getBarcode(), vehicle);
    }

    /**
     * Finds a vehicle by barcode.
     *
     * @param barcode unit barcode
     * @return vehicle instance
     * @throws VehicleNotFoundException if absent
     */
    public Vehicle findByBarcode(String barcode) {
        Vehicle vehicle = vehicles.get(barcode);
        if (vehicle == null) {
            throw new VehicleNotFoundException(barcode);
        }
        return vehicle;
    }

    /**
     * Returns all vehicles optionally filtered by type and branch.
     *
     * @param type     nullable type filter
     * @param branchId nullable branch filter
     * @return matching vehicles
     */
    public List<Vehicle> search(VehicleType type, String branchId) {
        return vehicles.values().stream()
                .filter(v -> type == null || v.getType() == type)
                .filter(v -> branchId == null || branchId.equals(v.getBranchId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** @return snapshot of all vehicles */
    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicles.values());
    }
}
