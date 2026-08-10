package com.andr1chkol.taskapi;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskStatusRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.andr1chkol.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TaskApiIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }

    @Test
    void createTask_whenRequestIsValid_returnsCreatedAndPersistsTask() throws Exception {
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("Learn JUnit", "Integration test");
        String json = objectMapper.writeValueAsString(createTaskRequest);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Learn JUnit"))
                .andExpect(jsonPath("$.description").value("Integration test"))
                .andExpect(jsonPath("$.status").value(TaskStatus.TODO.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        assertEquals(1, taskRepository.count());

        Task task = taskRepository.findAll().getFirst();

        assertNotNull(task.getId());
        assertEquals(createTaskRequest.getTitle(), task.getTitle());
        assertEquals(createTaskRequest.getDescription(), task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    void getTaskById_whenTaskExists_returnsOkAndPersistedTask() throws Exception {
        Task task = new Task("Learn JUnit", "Integration test");

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();
        assertNotNull(id);

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Learn JUnit"))
                .andExpect(jsonPath("$.description").value("Integration test"))
                .andExpect(jsonPath("$.status").value(TaskStatus.TODO.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updateTaskStatus_whenRequestIsValid_updatesPersistedTask() throws Exception {
        Task task = new Task("Learn JUnit", "Integration test");

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();

        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        String json = objectMapper.writeValueAsString(updateTaskStatusRequest);

        mockMvc.perform(patch("/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Learn JUnit"))
                .andExpect(jsonPath("$.description").value("Integration test"))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        Task updatedTask = taskRepository.findById(id).orElseThrow();

        assertEquals(task.getTitle(), updatedTask.getTitle());
        assertEquals(task.getDescription(), updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void deleteTask_whenTaskExists_returnsNoContentAndDeletesPersistedTask() throws Exception {
        Task task = new Task("Learn JUnit", "Integration test");

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertTrue(taskRepository.findById(id).isEmpty());
        assertEquals(0, taskRepository.count());
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_returnsNotFound() throws Exception {
        Long id = 999L;
        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));

        assertEquals(0, taskRepository.count());
    }
}