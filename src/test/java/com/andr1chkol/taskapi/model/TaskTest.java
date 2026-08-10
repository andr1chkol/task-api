package com.andr1chkol.taskapi.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskTest {
    @Test
    void constructor_setsProvidedFields() {
        String title = "Learn JUnit";
        String description = "Complete testing block";
        Task task = new Task(title, description);

        assertEquals(title, task.getTitle());
        assertEquals(description, task.getDescription());
    }

    @Test
    void constructor_setsDefaultStatusToTodo() {
        String title = "Learn JUnit";
        String description = "Test model";

        Task task = new Task(title, description);
        assertEquals(TaskStatus.TODO, task.getStatus());
    }

    @Test
    void constructor_initializesTimestamps() {
        String title = "Learn JUnit";
        String description = "Test model";

        Task task = new Task(title, description);
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
        assertEquals(task.getCreatedAt(), task.getUpdatedAt());
    }
}
