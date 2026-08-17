package com.hotel.lld.service;

import java.time.LocalDateTime;

/**
 * Housekeeping task entity — housekeeping is a workflow, not a method.
 */
public class HouseKeepingTask {
    private final String taskId;
    private final String roomNumber;
    private TaskStatus status;
    private final LocalDateTime assignedAt;
    private final String staffId;
    private LocalDateTime completedAt;

    public HouseKeepingTask(String taskId, String roomNumber, String staffId) {
        this.taskId = taskId;
        this.roomNumber = roomNumber;
        this.staffId = staffId;
        this.status = TaskStatus.PENDING;
        this.assignedAt = LocalDateTime.now();
    }

    public String getTaskId() {
        return taskId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public String getStaffId() {
        return staffId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
