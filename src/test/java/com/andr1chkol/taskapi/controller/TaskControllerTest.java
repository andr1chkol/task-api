package com.andr1chkol.taskapi.controller;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskStatusRequest;
import com.andr1chkol.taskapi.exception.TaskNotFoundException;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTaskById_whenTaskExists_returnsOkAndTaskJson() throws Exception {
        Long id = 1L;
        String title = "Learn JUnit";
        String description = "Test controller";

        Task task = new Task(title, description);

        ReflectionTestUtils.setField(task, "id", id);

        when(taskService.getTaskById(id)).thenReturn(task);

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.status").value(task.getStatus().name()));

        verify(taskService).getTaskById(id);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_returnsNotFound() throws Exception {
        Long id = 999L;
        when(taskService.getTaskById(id)).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));

        verify(taskService).getTaskById(id);
    }

    @Test
    void getAllTasks_withDefaultParameters_returnsOkAndPageJson() throws Exception {
        Sort.Direction direction = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Long id = 1L;

        Task task = new Task("Learn JUnit", "Test controller");
        ReflectionTestUtils.setField(task, "id", id);

        Pageable pageable = PageRequest.of(page, size);

        Page<Task> taskPage = new PageImpl<>(
                List.of(task),
                pageable,
                1
        );

        when(taskService.getAllTasks(null, direction, page, size)).thenReturn(taskPage);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].title").value(task.getTitle()))
                .andExpect(jsonPath("$.content[0].description").value(task.getDescription()))
                .andExpect(jsonPath("$.content[0].status").value(task.getStatus().name()))

                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));

        verify(taskService).getAllTasks(null, direction, page, size);
    }

    @Test
    void getAllTasks_withQueryParameters_passesParametersAndReturnsPage() throws Exception {
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Sort.Direction direction = Sort.Direction.ASC;
        int page = 1;
        int size = 5;

        Long id = 1L;
        Task task = new Task("Learn JUnit", "Test controller");
        ReflectionTestUtils.setField(task, "id", id);
        task.setStatus(status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Page<Task> taskPage = new PageImpl<>(List.of(task), pageable, 6);

        when(taskService.getAllTasks(status, direction, page, size)).thenReturn(taskPage);

        mockMvc.perform(get("/tasks")
                        .param("status", status.name())
                        .param("direction", direction.name())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))

                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].title").value(task.getTitle()))
                .andExpect(jsonPath("$.content[0].description").value(task.getDescription()))
                .andExpect(jsonPath("$.content[0].status").value(task.getStatus().name()))

                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));

        verify(taskService).getAllTasks(status, direction, page, size);
    }

    @Test
    void getAllTasks_whenStatusInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value 'INVALID' for parameter 'status'"))
                .andExpect(jsonPath("$.path").value("/tasks"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getAllTasks_whenSizeIsLessThanOne_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.size").value("Size must be at least 1"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getAllTasks_whenSizeExceedsMaximum_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.size").value("Size must be at most 100"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getAllTasks_whenPageIsNegative_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.page").value("Page must be 0 or greater"));

        verifyNoInteractions(taskService);
    }

    @Test
    void getAllTasks_whenDirectionIsInvalid_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("direction", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid value 'INVALID' for parameter 'direction'"))
                .andExpect(jsonPath("$.path").value("/tasks"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_whenRequestIsValid_returnsCreatedAndTaskJson() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("Learn JUnit", "Test controller");

        Long id = 1L;

        Task task = new Task(createTaskRequest.getTitle(), createTaskRequest.getDescription());
        ReflectionTestUtils.setField(task, "id", id);

        String json = objectMapper.writeValueAsString(createTaskRequest);

        when(taskService.createTask(any(CreateTaskRequest.class))).thenReturn(task);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(createTaskRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(createTaskRequest.getDescription()))
                .andExpect(jsonPath("$.status").value(task.getStatus().name()));

        ArgumentCaptor<CreateTaskRequest> captor = ArgumentCaptor.forClass(CreateTaskRequest.class);

        verify(taskService).createTask(captor.capture());

        CreateTaskRequest request = captor.getValue();

        assertEquals(createTaskRequest.getTitle(), request.getTitle());
        assertEquals(createTaskRequest.getDescription(), request.getDescription());

    }

    @Test
    void createTask_whenTitleIsBlank_returnsBadRequest() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest(" ", "Test controller");

        String json = objectMapper.writeValueAsString(createTaskRequest);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be blank"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_whenTitleExceedsMaximum_returnsBadRequest() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("A".repeat(101), "Test controller");
        String json = objectMapper.writeValueAsString(createTaskRequest);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not exceed 100 characters"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_whenDescriptionExceedsMaximum_returnsBadRequest() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("Learn JUnit", "A".repeat(1001));
        String json = objectMapper.writeValueAsString(createTaskRequest);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.fieldErrors.description").value("Description must not exceed 1000 characters"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createTask_whenJsonIsMalformed_returnsBadRequest() throws Exception {
        String malformedJson = """
                {
                  "title": "Learn JUnit",
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is invalid or contains unsupported values"))
                .andExpect(jsonPath("$.path").value("/tasks"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_whenRequestIsValid_returnsOkAndUpdatedTaskJson() throws Exception {
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("Updated title", "Updated description", TaskStatus.IN_PROGRESS);

        Long id = 1L;
        Task task = new Task(updateTaskRequest.getTitle(), updateTaskRequest.getDescription());
        ReflectionTestUtils.setField(task, "id", id);
        task.setStatus(updateTaskRequest.getStatus());

        String json = objectMapper.writeValueAsString(updateTaskRequest);

        when(taskService.updateTask(eq(id), any(UpdateTaskRequest.class))).thenReturn(task);

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.title").value(updateTaskRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(updateTaskRequest.getDescription()));

        ArgumentCaptor<UpdateTaskRequest> captor = ArgumentCaptor.forClass(UpdateTaskRequest.class);

        verify(taskService).updateTask(eq(id), captor.capture());

        assertEquals(updateTaskRequest.getTitle(), captor.getValue().getTitle());
        assertEquals(updateTaskRequest.getDescription(), captor.getValue().getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, captor.getValue().getStatus());
    }

    @Test
    void updateTask_whenTitleIsBlank_returnsBadRequest() throws Exception {
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest(" ", "Test controller", TaskStatus.IN_PROGRESS);

        String json = objectMapper.writeValueAsString(updateTaskRequest);

        mockMvc.perform(put("/tasks/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks/" + 1))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not be blank"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_whenTitleExceedsMaximum_returnsBadRequest() throws Exception {
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("A".repeat(101), "Test controller", TaskStatus.IN_PROGRESS);
        String json = objectMapper.writeValueAsString(updateTaskRequest);

        mockMvc.perform(put("/tasks/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks/" + 1))
                .andExpect(jsonPath("$.fieldErrors.title").value("Title must not exceed 100 characters"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_whenDescriptionExceedsMaximum_returnsBadRequest() throws Exception {
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("Learn JUnit", "A".repeat(1001), TaskStatus.IN_PROGRESS);
        String json = objectMapper.writeValueAsString(updateTaskRequest);

        mockMvc.perform(put("/tasks/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks/" + 1))
                .andExpect(jsonPath("$.fieldErrors.description").value("Description must not exceed 1000 characters"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTask_whenTaskDoesNotExists_returnsNotFound() throws Exception {
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("Updated title", "Updated description", TaskStatus.IN_PROGRESS);

        String json = objectMapper.writeValueAsString(updateTaskRequest);

        Long id = 999L;

        when(taskService.updateTask(eq(id), any(UpdateTaskRequest.class))).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));

        verify(taskService).updateTask(eq(id), any(UpdateTaskRequest.class));
    }

    @Test
    void updateTaskStatus_whenRequestIsValid_returnsOkAndUpdatedTaskJson() throws Exception {
        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);

        Long id = 1L;
        Task task = new Task("Learn JUnit", "Test controller");
        task.setStatus(TaskStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(task, "id", id);

        when(taskService.updateTaskStatus(id, TaskStatus.IN_PROGRESS)).thenReturn(task);

        String json = objectMapper.writeValueAsString(updateTaskStatusRequest);

        mockMvc.perform(patch("/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.title").value("Learn JUnit"))
                .andExpect(jsonPath("$.description").value("Test controller"));

        verify(taskService).updateTaskStatus(id, TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateTaskStatus_whenStatusIsInvalid_returnsBadRequest() throws Exception {
        String json = """
                {
                  "status": "INVALID"
                }
                """;

        mockMvc.perform(patch("/tasks/{id}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request body is invalid or contains unsupported values"))
                .andExpect(jsonPath("$.path").value("/tasks/" + 1 + "/status"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTaskStatus_whenStatusIsNull_returnsBadRequest() throws Exception {
        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(null);
        String json = objectMapper.writeValueAsString(updateTaskStatusRequest);

        mockMvc.perform(patch("/tasks/{id}/status", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/tasks/" + 1 + "/status"))
                .andExpect(jsonPath("$.fieldErrors.status").value("Status must not be null"));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateTaskStatus_whenTaskDoesNotExist_returnsNotFound() throws Exception {
        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);

        String json = objectMapper.writeValueAsString(updateTaskStatusRequest);

        Long id = 999L;

        when(taskService.updateTaskStatus(id, TaskStatus.IN_PROGRESS)).thenThrow(new TaskNotFoundException(id));

        mockMvc.perform(patch("/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id + "/status"));

        verify(taskService).updateTaskStatus(id, TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_whenTaskExists_returnsNoContent() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).deleteTaskById(id);
    }

    @Test
    void deleteTask_whenTaskDoesNotExist_returnsNotFound() throws Exception {
        Long id = 999L;

        doThrow(new TaskNotFoundException(id))
                .when(taskService)
                .deleteTaskById(id);

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));

        verify(taskService).deleteTaskById(id);
    }
}
