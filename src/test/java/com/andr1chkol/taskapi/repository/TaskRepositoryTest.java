package com.andr1chkol.taskapi.repository;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class TaskRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByIdAndOwner_whenTaskBelongsToOwner_returnsPersistedTask() {
        User owner = persistUser("andrew@example.com");
        Task task = createTask("Learn JUnit", "Test repository", owner);

        taskRepository.save(task);
        Long id = task.getId();

        entityManager.flush();
        entityManager.clear();

        Task foundTask = taskRepository.findByIdAndOwner(id, owner).orElseThrow();

        assertNotNull(id);
        assertEquals(task.getId(), foundTask.getId());
        assertEquals(task.getTitle(), foundTask.getTitle());
        assertEquals(task.getDescription(), foundTask.getDescription());
        assertEquals(task.getStatus(), foundTask.getStatus());
        assertEquals(owner.getId(), foundTask.getOwner().getId());
        assertNotNull(foundTask.getCreatedAt());
        assertNotNull(foundTask.getUpdatedAt());
    }

    @Test
    void findByIdAndOwner_whenTaskBelongsToAnotherUser_returnsEmpty() {
        User owner = persistUser("owner@example.com");
        User anotherUser = persistUser("another@example.com");
        Task task = createTask("Private task", "Owner only", owner);
        taskRepository.save(task);

        entityManager.flush();
        entityManager.clear();

        assertTrue(taskRepository.findByIdAndOwner(task.getId(), anotherUser).isEmpty());
    }

    @Test
    void findAllByOwner_whenTasksHaveDifferentOwners_returnsOnlyOwnerTasks() {
        User owner = persistUser("owner@example.com");
        User anotherUser = persistUser("another@example.com");

        taskRepository.save(createTask("Owner task 1", "Description", owner));
        taskRepository.save(createTask("Owner task 2", "Description", owner));
        taskRepository.save(createTask("Another task", "Description", anotherUser));

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findAllByOwner(owner, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(task -> task.getOwner().getId().equals(owner.getId())));
    }

    @Test
    void findAllByOwnerAndStatus_whenMatchingTasksExist_returnsOnlyOwnerTasksWithRequestedStatus() {
        User owner = persistUser("owner@example.com");
        User anotherUser = persistUser("another@example.com");

        Task task1 = createTask("Task 1", "Test repository", owner);
        task1.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task1);

        Task task2 = createTask("Task 2", "Test repository", owner);
        task2.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task2);

        Task task3 = createTask("Task 3", "Test repository", owner);
        task3.setStatus(TaskStatus.DONE);
        taskRepository.save(task3);

        Task anotherUserTask = createTask("Another task", "Test repository", anotherUser);
        anotherUserTask.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(anotherUserTask);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findAllByOwnerAndStatus(
                owner,
                TaskStatus.IN_PROGRESS,
                PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());

        assertTrue(result.getContent().stream().allMatch(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS)));
        assertTrue(result.getContent().stream().allMatch(task -> task.getOwner().getId().equals(owner.getId())));
    }

    @Test
    void findAllByOwnerAndStatus_whenSecondPageRequested_returnsCorrectPage() {
        User owner = persistUser("owner@example.com");

        for (int i = 1; i <= 5; i++) {
            Task task = createTask("Task " + i, "Test repository", owner);
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findAllByOwnerAndStatus(
                owner,
                TaskStatus.IN_PROGRESS,
                PageRequest.of(1, 2)
        );

        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());

        assertTrue(result.getContent().stream().allMatch(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS)));
    }

    @Test
    void findAllByOwnerAndStatus_whenSortedByCreatedAtDesc_returnsNewestFirst() {
        User owner = persistUser("owner@example.com");

        Task oldestTask = createTask("Oldest task", "Description", owner);
        oldestTask.setStatus(TaskStatus.IN_PROGRESS);
        oldestTask.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Task middleTask = createTask("Middle task", "Description", owner);
        middleTask.setStatus(TaskStatus.IN_PROGRESS);
        middleTask.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));

        Task newestTask = createTask("Newest task", "Description", owner);
        newestTask.setStatus(TaskStatus.IN_PROGRESS);
        newestTask.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));

        taskRepository.save(middleTask);
        taskRepository.save(newestTask);
        taskRepository.save(oldestTask);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findAllByOwnerAndStatus(
                owner,
                TaskStatus.IN_PROGRESS,
                PageRequest.of(0, 10, Sort.by("createdAt").descending())
        );

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(3, result.getContent().size());
        assertEquals("Newest task", result.getContent().get(0).getTitle());
        assertEquals("Middle task", result.getContent().get(1).getTitle());
        assertEquals("Oldest task", result.getContent().get(2).getTitle());
    }

    @Test
    void findAllByOwnerAndStatus_whenNoMatchingTasksExists_returnsEmptyPage() {
        User owner = persistUser("owner@example.com");
        User anotherUser = persistUser("another@example.com");

        Task task1 = createTask("Learn JUnit", "Test repository", owner);
        task1.setStatus(TaskStatus.DONE);
        Task task2 = createTask("Learn JUnit", "Test repository", anotherUser);
        task2.setStatus(TaskStatus.IN_PROGRESS);

        taskRepository.save(task1);
        taskRepository.save(task2);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findAllByOwnerAndStatus(
                owner,
                TaskStatus.IN_PROGRESS,
                PageRequest.of(0, 10)
        );

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getContent().size());
        assertTrue(result.isEmpty());
    }

    private User persistUser(String email) {
        User user = new User(email, "encoded-password");
        entityManager.persist(user);
        return user;
    }

    private Task createTask(String title, String description, User owner) {
        Task task = new Task(title, description);
        task.setOwner(owner);
        return task;
    }
}
