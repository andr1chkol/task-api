package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Data required to update an existing task")
public class UpdateTaskRequest {

    @Schema(
            description = "Updated task title",
            example = "Learn Spring Security"
    )
    @NotBlank(message = "Title must not be blank")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Schema(
            description = "Updated task description",
            example = "Start the authentication and authorization block"
    )
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Schema(
            description = "Updated task status",
            example = "IN_PROGRESS"
    )
    @NotNull(message = "Status must not be null")
    private TaskStatus status;


    public UpdateTaskRequest() {
    }

    public UpdateTaskRequest(String title, String description, TaskStatus status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
