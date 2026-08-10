package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.exception.TaskNotFoundException;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.repository.TaskRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(100, taskRepository);
    }

    @Test
    void getTaskById_whenTaskExists_returnsTask() {
        Long id = 1L;
        Task task = new Task("Learn JUnit", "Test service");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(id);

        assertSame(task, result);
        verify(taskRepository).findById(id);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(id));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(taskRepository).findById(id);
    }

    @Test
    void getAllTasks_whenStatusNull_returnsAllTasks() {
        Sort.Direction direction = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Task task = new Task("Learn JUnit", "Test service");

        Page<Task> expectedPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(any(Pageable.class)))
                .thenReturn(expectedPage);

        Page<Task> result = taskService.getAllTasks(null, direction, page, size);

        assertSame(expectedPage, result);

        verify(taskRepository).findAll(any(Pageable.class));
        verify(taskRepository, never()).findByStatus(any(), any(Pageable.class));
    }

    @Test
    void getAllTasks_whenStatusProvided_returnsTasksWithStatus() {
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Sort.Direction direction = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Task task = new Task("Learn JUnit", "Test service");
        task.setStatus(status);

        Page<Task> expectedPage = new PageImpl<>(List.of(task));

        when(taskRepository.findByStatus(status, expectedPageable))
                .thenReturn(expectedPage);

        Page<Task> result = taskService.getAllTasks(status, direction, page, size);

        assertSame(expectedPage, result);

        verify(taskRepository).findByStatus(status, expectedPageable);
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void createTask_whenRequestProvided_savesAndReturnsTask() {
        String title = "Learn JUnit";
        String description = "Test service";

        Task expectedTask = new Task(title, description);
        CreateTaskRequest request = new CreateTaskRequest(title, description);

        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

        Task result = taskService.createTask(request);
        assertSame(expectedTask, result);

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        verify(taskRepository).save(taskCaptor.capture());

        assertEquals(title, taskCaptor.getValue().getTitle());
        assertEquals(description, taskCaptor.getValue().getDescription());
        assertEquals(TaskStatus.TODO, taskCaptor.getValue().getStatus());
    }

    @Test
    void updateTask_whenTaskExists_updatesAndReturnsTask() {
        Long id = 1L;

        Task task = new Task("Old title", "Old description");
        Instant oldUpdatedAt = Instant.parse("2020-01-01T00:00:00Z");
        task.setUpdatedAt(oldUpdatedAt);

        UpdateTaskRequest request = new UpdateTaskRequest("New title", "New description", TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.updateTask(id, request);

        assertSame(task, result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getStatus(), result.getStatus());
        assertTrue(result.getUpdatedAt().isAfter(oldUpdatedAt));

        verify(taskRepository).findById(id);
    }

    @Test
    void updateTask_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        UpdateTaskRequest request = new UpdateTaskRequest("New title", "New description", TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(id, request));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(taskRepository).findById(id);
    }

    @Test
    void updateTaskStatus_whenTaskExists_updatesStatusAndReturnsTask() {
        Long id = 1L;

        Task task = new Task("Learn JUnit", "Test service");
        Instant oldUpdatedAt = Instant.parse("2020-01-01T00:00:00Z");
        task.setUpdatedAt(oldUpdatedAt);

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        Task result = taskService.updateTaskStatus(id, TaskStatus.DONE);

        assertSame(task, result);
        assertEquals(TaskStatus.DONE, result.getStatus());
        assertTrue(result.getUpdatedAt().isAfter(oldUpdatedAt));

        verify(taskRepository).findById(id);
    }

    @Test
    void updateTaskStatus_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTaskStatus(id, TaskStatus.DONE));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(taskRepository).findById(id);
    }

    @Test
    void deleteTaskById_whenTaskExists_deletesTask() {
        Long id = 1L;

        Task task = new Task("Learn JUnit", "Test service");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        taskService.deleteTaskById(id);

        verify(taskRepository).findById(id);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTaskById_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTaskById(id));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(taskRepository).findById(id);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
