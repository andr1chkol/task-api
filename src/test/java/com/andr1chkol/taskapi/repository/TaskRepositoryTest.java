package com.andr1chkol.taskapi.repository;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
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
    void saveAndFindById_whenTaskIsValid_returnsPersistedTask() {
        Task task = new Task("Learn JUnit", "Test repository");

        taskRepository.save(task);
        Long id = task.getId();

        entityManager.flush();
        entityManager.clear();

        Task foundTask = taskRepository.findById(id).orElseThrow();

        assertNotNull(id);
        assertEquals(task.getId(), foundTask.getId());
        assertEquals(task.getTitle(), foundTask.getTitle());
        assertEquals(task.getDescription(), foundTask.getDescription());
        assertEquals(task.getStatus(), foundTask.getStatus());
        assertNotNull(foundTask.getCreatedAt());
        assertNotNull(foundTask.getUpdatedAt());
    }

    @Test
    void findByStatus_whenMatchingTasksExist_returnsOnlyTasksWithRequestedStatus() {
        Task task1 = new Task("Task 1", "Test repository");
        task1.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task1);

        Task task2 = new Task("Task 2", "Test repository");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task2);

        Task task3 = new Task("Task 3", "Test repository");
        task3.setStatus(TaskStatus.DONE);
        taskRepository.save(task3);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(2, result.getContent().size());

        assertTrue(result.getContent().stream().allMatch(task -> task.getStatus().equals(TaskStatus.IN_PROGRESS)));
    }

    @Test
    void findByStatus_whenSecondPageRequested_returnsCorrectPage() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task("Task " + i, "Test repository");
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS, PageRequest.of(1, 2));

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
    void findByStatus_whenSortedByCreatedAtDesc_returnsNewestFirst() {
        Task oldestTask = new Task("Oldest task", "Description");
        oldestTask.setStatus(TaskStatus.IN_PROGRESS);
        oldestTask.setCreatedAt(Instant.parse("2026-01-01T10:00:00Z"));

        Task middleTask = new Task("Middle task", "Description");
        middleTask.setStatus(TaskStatus.IN_PROGRESS);
        middleTask.setCreatedAt(Instant.parse("2026-02-01T10:00:00Z"));

        Task newestTask = new Task("Newest task", "Description");
        newestTask.setStatus(TaskStatus.IN_PROGRESS);
        newestTask.setCreatedAt(Instant.parse("2026-03-01T10:00:00Z"));

        taskRepository.save(middleTask);
        taskRepository.save(newestTask);
        taskRepository.save(oldestTask);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS,
                PageRequest.of(0, 10, Sort.by("createdAt").descending()));

        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(3, result.getContent().size());
        assertEquals("Newest task", result.getContent().get(0).getTitle());
        assertEquals("Middle task", result.getContent().get(1).getTitle());
        assertEquals("Oldest task", result.getContent().get(2).getTitle());
    }

    @Test
    void findByStatus_whenNoMatchingTasksExists_returnsEmptyPage() {
        Task task1 = new Task("Learn JUnit", "Test repository");
        task1.setStatus(TaskStatus.DONE);
        Task task2 = new Task("Learn JUnit", "Test repository");

        taskRepository.save(task1);
        taskRepository.save(task2);

        entityManager.flush();
        entityManager.clear();

        Page<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS, PageRequest.of(0, 10));

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getContent().size());
        assertTrue(result.isEmpty());
    }
}