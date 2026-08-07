package com.carrental.lld.log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Separate service for vehicle event logging and search.
 * <p>
 * Why: logging is a cross-cutting concern; keeping it out of {@code Vehicle}
 * avoids bloating the entity and enables independent query APIs.
 */
public class VehicleLogService {
    private final List<VehicleLog> logs = new CopyOnWriteArrayList<>();

    /**
     * Records a new log entry.
     *
     * @param vehicleBarcode vehicle id
     * @param type           event type
     * @param description    detail
     * @param performedBy    actor
     * @return persisted log entry
     */
    public VehicleLog addLog(String vehicleBarcode, VehicleLogType type,
                             String description, String performedBy) {
        VehicleLog log = new VehicleLog(vehicleBarcode, type, description, performedBy);
        logs.add(log);
        return log;
    }

    /**
     * Searches logs by vehicle, optional date range, and optional type.
     *
     * @param vehicleBarcode required vehicle filter
     * @param from           inclusive start (nullable)
     * @param to             inclusive end (nullable)
     * @param type           optional type filter
     * @return matching logs sorted by timestamp
     */
    public List<VehicleLog> search(String vehicleBarcode, Instant from, Instant to, VehicleLogType type) {
        return logs.stream()
                .filter(l -> l.getVehicleBarcode().equals(vehicleBarcode))
                .filter(l -> from == null || !l.getTimestamp().isBefore(from))
                .filter(l -> to == null || !l.getTimestamp().isAfter(to))
                .filter(l -> type == null || l.getType() == type)
                .sorted(Comparator.comparing(VehicleLog::getTimestamp))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
