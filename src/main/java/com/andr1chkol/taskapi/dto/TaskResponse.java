package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Task returned by the API")
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "createdAt",
        "updatedAt"
})

public class TaskResponse {
    @Schema(
            description = "Unique task identifier",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final Long id;

    @Schema(
            description = "Task title",
            example = "Learn Spring Boot"
    )
    private final String title;

    @Schema(
            description = "Task description",
            example = "Test service"
    )
    private final String description;

    @Schema(
            description = "Current task status",
            example = "IN_PROGRESS"
    )
    private final TaskStatus status;

    @Schema(
            description = "Task creation timestamp",
            example = "2026-08-13T18:30:00Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private final Instant createdAt;

    @Schema(
            description = "Timestamp of the latest task update",
            example = "2026-08-13T19:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
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
