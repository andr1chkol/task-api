package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data required to change task status")
public class UpdateTaskStatusRequest {
    @Schema(
            description = "New task status",
            example = "DONE"
    )
    @NotNull(message = "Status must not be null")
    private TaskStatus status;

    public UpdateTaskStatusRequest() {
    }

    public UpdateTaskStatusRequest(TaskStatus status) {
        this.status = status;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
