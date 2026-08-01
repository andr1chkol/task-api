package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.exception.TaskNotFoundException;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();
    private final int maxTasks;

    public TaskService(@Value("${task-api.max-tasks:100}") int maxTasks) {
        this.maxTasks = maxTasks;
        log.info("Task service initialized with maxTasks={}", maxTasks);

        Task task = new Task("Learn Spring Boot", "Create TaskApi web");
        long id = idGenerator.incrementAndGet();
        task.setId(id);

        tasks.put(id, task);
    }

    public List<Task> getAllTasks() {
        log.debug("Getting all tasks, current count={}", tasks.size());
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Long id) {
        log.debug("Getting task with id={}", id);
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public Task createTask(CreateTaskRequest taskRequest) {
        Long id = idGenerator.incrementAndGet();
        Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());

        task.setId(id);
        task.setStatus(TaskStatus.TODO);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        tasks.put(id, task);
        log.info("Task created with id={}", id);
        return task;
    }

    public Task updateTask(Long id, UpdateTaskRequest updateTaskRequest) {
        Task task = tasks.get(id);

        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        task.setTitle(updateTaskRequest.getTitle());
        task.setDescription(updateTaskRequest.getDescription());
        task.setStatus(updateTaskRequest.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        tasks.put(id, task);
        log.debug("Task updated with id={}", id);
        return task;
    }

    public Task updateTaskStatus(Long id, TaskStatus newStatus) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        tasks.put(id, task);
        log.debug("Task status updated with id={}, status={}", id, newStatus);
        return task;
    }

    public void deleteTaskById(Long id) {
        Task removedTask = tasks.remove(id);

        if (removedTask == null) {
            throw new TaskNotFoundException(id);
        }
        log.info("Task deleted with id={}", id);
    }
}
