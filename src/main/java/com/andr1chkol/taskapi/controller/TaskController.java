package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return service.getAllTasks();
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = service.getTaskById(id);
        if (task.isPresent()) {
            return ResponseEntity.ok(task.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/tasks")
    public Task createTask(@RequestBody Task task) {
        return service.createTask(task);
    }
}
