package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskController {
    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 2L;

    public TaskController() {
        Task task = new Task("Learn Spring Boot", "Create TaskApi web");
        task.setId(1L);

        tasks.add(task);
    }

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return tasks;
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {
        task.setId(nextId++);
        task.setStatus(TaskStatus.TODO);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        tasks.add(task);
        return task;
    }
}
