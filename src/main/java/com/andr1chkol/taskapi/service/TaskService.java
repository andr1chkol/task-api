package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.dto.CreateTaskRequest;
import com.andr1chkol.taskapi.dto.UpdateTaskRequest;
import com.andr1chkol.taskapi.exception.TaskNotFoundException;
import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.repository.TaskRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
@Transactional(readOnly = true)
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final int maxTasks;

    public TaskService(@Value("${task-api.max-tasks:100}") int maxTasks, TaskRepository taskRepository) {
        this.maxTasks = maxTasks;
        this.taskRepository = taskRepository;

        log.info("Task service initialized with maxTasks={}", maxTasks);
    }

    public List<Task> getAllTasks() {
        List<Task> foundTasks = taskRepository.findAll();
        log.debug("Getting all tasks, current count={}", foundTasks.size());
        return foundTasks;
    }

    public Task getTaskById(Long id) {
        log.debug("Getting task with id={}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

    }

    @Transactional
    public Task createTask(CreateTaskRequest taskRequest) {
        Task task = new Task(taskRequest.getTitle(), taskRequest.getDescription());

        Task savedTask = taskRepository.save(task);
        log.info("Task created with id={}", savedTask.getId());
        return savedTask;
    }

    @Transactional
    public Task updateTask(Long id, UpdateTaskRequest updateTaskRequest) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        task.setTitle(updateTaskRequest.getTitle());
        task.setDescription(updateTaskRequest.getDescription());
        task.setStatus(updateTaskRequest.getStatus());
        task.setUpdatedAt(Instant.now());

        log.debug("Task updated with id={}", id);
        return task;
    }

    @Transactional
    public Task updateTaskStatus(Long id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(newStatus);
        task.setUpdatedAt(Instant.now());

        log.debug("Task status updated with id={}, status={}", id, newStatus);
        return task;
    }

    @Transactional
    public void deleteTaskById(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        taskRepository.delete(task);
        log.info("Task deleted with id={}", id);
    }
}
