package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.TaskResponse;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskStatusRequest;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.service.TaskService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/tasks")
@RestController
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping()
    public List<TaskResponse> getAllTasks() {
        return service.getAllTasks().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Task task = service.getTaskById(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PostMapping()
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest taskRequest) {
        Task task = service.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                   @RequestBody @Valid UpdateTaskRequest taskRequest) {
        Task task = service.updateTask(id, taskRequest);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
                                                         @RequestBody @Valid UpdateTaskStatusRequest taskRequest) {
        Task task = service.updateTaskStatus(id, taskRequest.getStatus());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        service.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
