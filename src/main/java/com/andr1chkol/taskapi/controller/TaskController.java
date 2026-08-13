package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.dto.*;
import com.andr1chkol.taskapi.exception.ApiError;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/tasks")
@RestController
@Tag(
        name = "Tasks",
        description = "Operations for managing tasks"
)
public class TaskController {
    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @Operation(
            summary = "Get all tasks",
            description = "Returns a paginated list of tasks with optional status filtering and sorting"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks returned successfully",
                    content = @Content(
                            schema = @Schema(implementation = PageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filtering, sorting or pagination parameter",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })

    @GetMapping()
    public PageResponse<TaskResponse> getAllTasks(
            @Parameter(
                    description = "Filter tasks by status",
                    example = "TODO")
            @RequestParam(required = false) TaskStatus status,
            @Parameter(
                    description = "Sorting direction by creation time",
                    example = "DESC")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @Parameter(
                    description = "Zero-based page number",
                    example = "0")
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page must be 0 or greater") int page,
            @Parameter(
                    description = "Number of tasks per page",
                    example = "10")
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size must be at most 100") int size
    ) {
        return PageResponse.from(service.getAllTasks(status, direction, page, size).map(TaskResponse::from));
    }

    @Operation(
            summary = "Get task by ID",
            description = "Returns a single task using its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task found",
                    content = @Content(
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(
                    description = "Unique task identifier",
                    example = "1")
            @PathVariable Long id
    ) {
        Task task = service.getTaskById(id);
        return ResponseEntity.ok(TaskResponse.from(task));
    }


    @Operation(
            summary = "Create a task",
            description = "Creates a new task from the provided data"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body is invalid",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PostMapping()
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody @Valid CreateTaskRequest taskRequest
    ) {
        Task task = service.createTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @Operation(
            summary = "Update a task",
            description = "Replaces the editable data of an existing task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task ID or request body is invalid",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(
                    description = "Unique task identifier",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody @Valid UpdateTaskRequest taskRequest
    ) {
        Task task = service.updateTask(id, taskRequest);
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @Operation(
            summary = "Update task status",
            description = "Changes only the status of an existing task"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Task status updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Task ID or status is invalid",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @Parameter(
                    description = "Unique task identifier",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody @Valid UpdateTaskStatusRequest taskRequest
    ) {
        Task task = service.updateTaskStatus(id, taskRequest.getStatus());
        return ResponseEntity.ok(TaskResponse.from(task));
    }

    @Operation(
            summary = "Delete a task",
            description = "Deletes an existing task using its unique identifier"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Task deleted successfully",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid task ID",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Task not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(
                    description = "Unique task identifier",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        service.deleteTaskById(id);
        return ResponseEntity.noContent().build();
    }
}
