package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;

import java.time.LocalDateTime;

public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;


    public TaskResponse(Long id, String title, String description, TaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(),
                task.getDescription(), task.getStatus(),
                task.getCreatedAt(), task.getUpdatedAt());
    }
}
