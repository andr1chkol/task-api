package com.andr1chkol.taskapi.dto;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskResponseTest {
    @Test
    void from_whenTaskProvided_mapsFieldsToResponse() {
        String title = "Learn JUnit";
        String description = "Test DTO mapping";

        Task task = new Task(title, description);

        task.setStatus(TaskStatus.IN_PROGRESS);

        Instant createdAt = task.getCreatedAt();
        Instant updatedAt = task.getUpdatedAt();

        TaskResponse taskResponse = TaskResponse.from(task);

        assertNotNull(taskResponse);
        assertEquals(title, taskResponse.getTitle());
        assertEquals(description, taskResponse.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, taskResponse.getStatus());
        assertEquals(createdAt, taskResponse.getCreatedAt());
        assertEquals(updatedAt, taskResponse.getUpdatedAt());
    }
}
