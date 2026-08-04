package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.TaskResponse;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskStatusRequest;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.service.TaskService;
import com.andr1chkol.taskapi.dto.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/tasks")
@RestController
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping()
    public PageResponse<TaskResponse> getAllTasks(@RequestParam(required = false) TaskStatus status,
                                          @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                                          @RequestParam(defaultValue = "0")
                                          @Min(value = 0, message = "Page must be 0 or greater") int page,
                                          @RequestParam(defaultValue = "10")
                                          @Min(value = 1, message = "Size must be at least 1")
                                          @Max(value = 100, message = "Size must be at most 100") int size) {
        return PageResponse.from(service.getAllTasks(status, direction, page, size).map(TaskResponse::from));
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
