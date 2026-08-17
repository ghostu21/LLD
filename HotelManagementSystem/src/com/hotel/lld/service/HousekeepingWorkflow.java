package com.hotel.lld.service;

import com.hotel.lld.room.Room;
import com.hotel.lld.room.RoomInventory;
import com.hotel.lld.room.RoomStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
        for (HouseKeepingTask existing : tasks.values()) {
            if (existing.getRoomNumber().equals(roomNumber)
                    && (existing.getStatus() == TaskStatus.PENDING
                    || existing.getStatus() == TaskStatus.IN_PROGRESS)) {
                return existing;
            }
        }

        Room room = inventory.findByNumber(roomNumber);
        room.setStatus(RoomStatus.BEING_SERVICED);

        String taskId = "HK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        HouseKeepingTask task = new HouseKeepingTask(taskId, roomNumber, staffId);
        tasks.put(taskId, task);
        return task;
    }

    public void start(String taskId) {
        HouseKeepingTask task = require(taskId);
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Cannot start task in status " + task.getStatus());
        }
        task.setStatus(TaskStatus.IN_PROGRESS);
    }

    public void complete(String taskId) {
        HouseKeepingTask task = require(taskId);
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete task in status " + task.getStatus());
        }
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        Room room = inventory.findByNumber(task.getRoomNumber());
        room.markAvailableIfBeingServiced();
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
        result.sort(Comparator.comparing(HouseKeepingTask::getAssignedAt).reversed());
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
