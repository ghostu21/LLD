package com.hotel.lld.room;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory room repository with style + date search.
 */
public class RoomInventory {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public void add(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    public Room findByNumber(String roomNumber) {
        Room room = rooms.get(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomNumber);
        }
        return room;
    }

    public List<Room> all() {
        return new ArrayList<>(rooms.values());
    }

    /**
     * Searches inventory by style and date-range availability.
     */
    public List<Room> search(RoomStyle style, LocalDate start, int nights) {
        return rooms.values().stream()
                .filter(r -> r.getStyle() == style)
                .filter(r -> r.isAvailable(start, nights))
                .collect(Collectors.toList());
    }

    public List<Room> byHotel(String hotelId) {
        return rooms.values().stream()
                .filter(r -> r.getHotelId().equals(hotelId))
                .collect(Collectors.toList());
    }
}
