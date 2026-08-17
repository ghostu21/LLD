package com.hotel.lld.service;

import com.hotel.lld.room.Room;
import com.hotel.lld.room.RoomInventory;
import com.hotel.lld.room.RoomStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Task-based housekeeping workflow: CHECKOUT → BEING_SERVICED → AVAILABLE.
 */
public class HousekeepingWorkflow {
    private final RoomInventory inventory;
    private final Map<String, HouseKeepingTask> tasks = new ConcurrentHashMap<>();

    public HousekeepingWorkflow(RoomInventory inventory) {
        this.inventory = inventory;
    }

    public HouseKeepingTask assignAfterCheckout(String roomNumber, String staffId) {
        Room room = inventory.findByNumber(roomNumber);
        room.setStatus(RoomStatus.BEING_SERVICED);

        String taskId = "HK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        HouseKeepingTask task = new HouseKeepingTask(taskId, roomNumber, staffId);
        tasks.put(taskId, task);
        return task;
    }

    public void start(String taskId) {
        HouseKeepingTask task = require(taskId);
        task.setStatus(TaskStatus.IN_PROGRESS);
    }

    public void complete(String taskId) {
        HouseKeepingTask task = require(taskId);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        Room room = inventory.findByNumber(task.getRoomNumber());
        room.setStatus(RoomStatus.AVAILABLE);
    }

    public List<HouseKeepingTask> allTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<HouseKeepingTask> tasksForRoom(String roomNumber) {
        List<HouseKeepingTask> result = new ArrayList<>();
        for (HouseKeepingTask t : tasks.values()) {
            if (t.getRoomNumber().equals(roomNumber)) {
                result.add(t);
            }
        }
        return result;
    }

    private HouseKeepingTask require(String taskId) {
        HouseKeepingTask task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        return task;
    }
}
