package com.andr1chkol.taskapi;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskStatusRequest;
import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.repository.UserRepository;
import com.andr1chkol.taskapi.security.JwtService;
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
import com.andr1chkol.taskapi.model.Role;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "security.jwt.secret="
                + "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "security.jwt.expiration-ms=900000"
})
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createTask_whenRequestIsValid_returnsCreatedAndPersistsTask() throws Exception {
        TestUser currentUser = createTestUser("andrew@example.com");
        CreateTaskRequest createTaskRequest = new CreateTaskRequest("Learn JUnit", "Integration test");
        String json = objectMapper.writeValueAsString(createTaskRequest);

        mockMvc.perform(post("/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token()))
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
        assertEquals(currentUser.user().getId(), task.getOwner().getId());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getUpdatedAt());
    }

    @Test
    void getTaskById_whenTaskExists_returnsOkAndPersistedTask() throws Exception {
        TestUser currentUser = createTestUser("andrew@example.com");
        Task task = new Task("Learn JUnit", "Integration test");
        task.setOwner(currentUser.user());

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();
        assertNotNull(id);

        mockMvc.perform(get("/tasks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token())))
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
        TestUser currentUser = createTestUser("andrew@example.com");
        Task task = new Task("Learn JUnit", "Integration test");
        task.setOwner(currentUser.user());

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();

        UpdateTaskStatusRequest updateTaskStatusRequest = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);
        String json = objectMapper.writeValueAsString(updateTaskStatusRequest);

        mockMvc.perform(patch("/tasks/{id}/status", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token()))
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
        TestUser currentUser = createTestUser("andrew@example.com");
        Task task = new Task("Learn JUnit", "Integration test");
        task.setOwner(currentUser.user());

        Task savedTask = taskRepository.save(task);

        Long id = savedTask.getId();

        mockMvc.perform(delete("/tasks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertTrue(taskRepository.findById(id).isEmpty());
        assertEquals(0, taskRepository.count());
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_returnsNotFound() throws Exception {
        TestUser currentUser = createTestUser("andrew@example.com");
        Long id = 999L;
        mockMvc.perform(get("/tasks/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value("/tasks/" + id));

        assertEquals(0, taskRepository.count());
    }

    @Test
    void getTaskById_whenTaskBelongsToAnotherUser_returnsNotFound() throws Exception {
        TestUser owner = createTestUser("owner@example.com");
        TestUser anotherUser = createTestUser("another@example.com");

        Task task = new Task("Private task", "Owner only");
        task.setOwner(owner.user());
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(get("/tasks/{id}", savedTask.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/tasks/" + savedTask.getId()));
    }

    @Test
    void getTasks_whenUsersHaveDifferentTasks_returnsOnlyCurrentUserTasks() throws Exception {
        TestUser currentUser = createTestUser("andrew@example.com");
        TestUser anotherUser = createTestUser("another@example.com");

        Task ownTask = new Task("Own task", "Visible");
        ownTask.setOwner(currentUser.user());
        taskRepository.save(ownTask);

        Task foreignTask = new Task("Foreign task", "Hidden");
        foreignTask.setOwner(anotherUser.user());
        taskRepository.save(foreignTask);

        mockMvc.perform(get("/tasks")
                        .header(HttpHeaders.AUTHORIZATION, bearer(currentUser.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Own task"));
    }

    @Test
    void deleteTask_whenTaskBelongsToAnotherUser_returnsNotFoundAndKeepsTask() throws Exception {
        TestUser owner = createTestUser("owner@example.com");
        TestUser anotherUser = createTestUser("another@example.com");

        Task task = new Task("Private task", "Owner only");
        task.setOwner(owner.user());
        Task savedTask = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token())))
                .andExpect(status().isNotFound());

        assertTrue(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void updateTask_whenTaskBelongsToAnotherUser_returnsNotFoundAndKeepsTask() throws Exception {
        TestUser owner = createTestUser("owner@example.com");
        TestUser anotherUser = createTestUser("another@example.com");

        Task task = new Task("Original title", "Original description");
        task.setOwner(owner.user());
        Task savedTask = taskRepository.save(task);

        UpdateTaskRequest request = new UpdateTaskRequest(
                "Changed title",
                "Changed description",
                TaskStatus.DONE
        );

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        Task unchangedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertEquals("Original title", unchangedTask.getTitle());
        assertEquals("Original description", unchangedTask.getDescription());
        assertEquals(TaskStatus.TODO, unchangedTask.getStatus());
    }

    @Test
    void updateTaskStatus_whenTaskBelongsToAnotherUser_returnsNotFoundAndKeepsStatus() throws Exception {
        TestUser owner = createTestUser("owner@example.com");
        TestUser anotherUser = createTestUser("another@example.com");

        Task task = new Task("Private task", "Owner only");
        task.setOwner(owner.user());
        Task savedTask = taskRepository.save(task);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.DONE);

        mockMvc.perform(patch("/tasks/{id}/status", savedTask.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(anotherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        Task unchangedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertEquals(TaskStatus.TODO, unchangedTask.getStatus());
    }

    @Test
    void openApiDocs_describeBearerJwtForTasksAndKeepLoginPublic()
            throws Exception {

        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath(
                        "$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath(
                        "$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath(
                        "$.paths['/tasks'].get.security[0].bearerAuth").isArray())
                .andExpect(jsonPath(
                        "$.paths['/auth/login'].post.security").doesNotExist());
    }

    @Test
    void adminPing_whenTokenIsMissing_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.path").value("/admin/ping"));
    }

    @Test
    void adminPing_whenUserHasUserRole_returnsForbidden() throws Exception {
        TestUser user = createTestUser("user@example.com");

        mockMvc.perform(get("/admin/ping").header(
                        HttpHeaders.AUTHORIZATION,
                        bearer(user.token())
                ))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access is denied"))
                .andExpect(jsonPath("$.path").value("/admin/ping"));
    }

    @Test
    void adminPing_whenUserHasRoleAdmin_returnsOk() throws Exception {
        TestUser user = createAdminUser("admin@example.com");

        mockMvc.perform(get("/admin/ping").header(
                        HttpHeaders.AUTHORIZATION,
                        bearer(user.token())
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Admin access granted"));
    }

    private TestUser createTestUser(String email) {
        User user = new User(email, passwordEncoder.encode("qwerty123"));
        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());
        return new TestUser(savedUser, token);
    }

    private TestUser createAdminUser(String email) {
        TestUser admin = createTestUser(email);

        jdbcTemplate.update(
                "UPDATE users SET role = ? WHERE id = ?",
                Role.ADMIN.name(),
                admin.user().getId()
        );

        return admin;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record TestUser(User user, String token) {
    }
}
