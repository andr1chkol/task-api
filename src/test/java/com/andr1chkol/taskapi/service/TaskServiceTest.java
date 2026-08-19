package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.exception.TaskNotFoundException;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.repository.TaskRepository;

import com.andr1chkol.taskapi.model.User;
import com.andr1chkol.taskapi.security.CurrentUserService;
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

    @Mock
    private CurrentUserService currentUserService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User(
                "andrew@example.com",
                "encoded-password"
        );

        taskService = new TaskService(
                100,
                taskRepository,
                currentUserService
        );

        lenient()
                .when(currentUserService.getCurrentUser())
                .thenReturn(currentUser);
    }

    @Test
    void getTaskById_whenTaskExists_returnsTask() {
        Long id = 1L;
        Task task = new Task("Learn JUnit", "Test service");

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(id);

        assertSame(task, result);

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void getTaskById_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(id));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void getAllTasks_whenStatusNull_returnsCurrentUserTasks() {
        Sort.Direction direction = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Task task = new Task("Learn JUnit", "Test service");
        Page<Task> expectedPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAllByOwner(currentUser, expectedPageable)).thenReturn(expectedPage);

        Page<Task> result = taskService.getAllTasks(null, direction, page, size);

        assertSame(expectedPage, result);

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findAllByOwner(currentUser, expectedPageable);

        verify(taskRepository, never()).findAllByOwnerAndStatus(any(User.class), any(TaskStatus.class), any(Pageable.class));
    }

    @Test
    void getAllTasks_whenStatusProvided_returnsCurrentUserTasksWithStatus() {
        TaskStatus status = TaskStatus.IN_PROGRESS;
        Sort.Direction direction = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Pageable expectedPageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        Task task = new Task("Learn JUnit", "Test service");
        task.setStatus(status);

        Page<Task> expectedPage = new PageImpl<>(List.of(task));

        when(taskRepository.findAllByOwnerAndStatus(currentUser, status, expectedPageable)).thenReturn(expectedPage);

        Page<Task> result = taskService.getAllTasks(status, direction, page, size);

        assertSame(expectedPage, result);

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findAllByOwnerAndStatus(currentUser, status, expectedPageable);

        verify(taskRepository, never()).findAllByOwner(any(User.class), any(Pageable.class));
    }

    @Test
    void createTask_whenRequestProvided_assignsOwnerAndSavesTask() {
        String title = "Learn JUnit";
        String description = "Test service";

        Task expectedTask = new Task(title, description);
        CreateTaskRequest request = new CreateTaskRequest(title, description);

        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

        Task result = taskService.createTask(request);
        assertSame(expectedTask, result);

        ArgumentCaptor<Task> taskCaptor =
                ArgumentCaptor.forClass(Task.class);

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).save(taskCaptor.capture());

        Task taskToSave = taskCaptor.getValue();

        assertEquals(title, taskToSave.getTitle());
        assertEquals(description, taskToSave.getDescription());
        assertEquals(TaskStatus.TODO, taskToSave.getStatus());
        assertSame(currentUser, taskToSave.getOwner());
    }

    @Test
    void updateTask_whenTaskExists_updatesAndReturnsTask() {
        Long id = 1L;

        Task task = new Task("Old title", "Old description");
        Instant oldUpdatedAt = Instant.parse("2020-01-01T00:00:00Z");
        task.setUpdatedAt(oldUpdatedAt);

        UpdateTaskRequest request = new UpdateTaskRequest("New title", "New description", TaskStatus.IN_PROGRESS);

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.of(task));

        Task result = taskService.updateTask(id, request);

        assertSame(task, result);
        assertEquals(request.getTitle(), result.getTitle());
        assertEquals(request.getDescription(), result.getDescription());
        assertEquals(request.getStatus(), result.getStatus());
        assertTrue(result.getUpdatedAt().isAfter(oldUpdatedAt));

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void updateTask_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        UpdateTaskRequest request = new UpdateTaskRequest("New title", "New description", TaskStatus.IN_PROGRESS);

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTask(id, request));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void updateTaskStatus_whenTaskExists_updatesStatusAndReturnsTask() {
        Long id = 1L;

        Task task = new Task("Learn JUnit", "Test service");
        Instant oldUpdatedAt = Instant.parse("2020-01-01T00:00:00Z");
        task.setUpdatedAt(oldUpdatedAt);

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.of(task));

        Task result = taskService.updateTaskStatus(id, TaskStatus.DONE);

        assertSame(task, result);
        assertEquals(TaskStatus.DONE, result.getStatus());
        assertTrue(result.getUpdatedAt().isAfter(oldUpdatedAt));

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void updateTaskStatus_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.updateTaskStatus(id, TaskStatus.DONE));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
    }

    @Test
    void deleteTaskById_whenTaskExists_deletesTask() {
        Long id = 1L;

        Task task = new Task("Learn JUnit", "Test service");

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.of(task));

        taskService.deleteTaskById(id);

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTaskById_whenTaskDoesNotExist_throwsTaskNotFoundException() {
        Long id = 999L;

        when(taskRepository.findByIdAndOwner(id, currentUser)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTaskById(id));

        assertEquals("Task with id " + id + " not found", exception.getMessage());

        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(id, currentUser);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
