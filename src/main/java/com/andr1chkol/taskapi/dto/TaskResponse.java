package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;

@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "createdAt",
        "updatedAt"
})

public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;


    public TaskResponse(Long id, String title, String description, TaskStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(),
                task.getDescription(), task.getStatus(),
                task.getCreatedAt(), task.getUpdatedAt());
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
