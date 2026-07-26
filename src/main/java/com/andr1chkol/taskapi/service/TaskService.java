package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {
    private final Map<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public TaskService() {
        Task task = new Task("Learn Spring Boot", "Create TaskApi web");
        long id = idGenerator.incrementAndGet();
        task.setId(id);

        tasks.put(id, task);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Optional<Task> getTaskById(Long id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public Task createTask(Task task) {
        Long id = idGenerator.incrementAndGet();
        task.setId(id);
        task.setStatus(TaskStatus.TODO);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        tasks.put(id, task);
        return task;
    }

    public Optional<Task> updateTask(Long id, Task newData) {
        Task task = tasks.get(id);

        if (task == null) {
            return Optional.empty();
        }

        task.setTitle(newData.getTitle());
        task.setDescription(newData.getDescription());
        task.setStatus(newData.getStatus());
        task.setUpdatedAt(LocalDateTime.now());

        tasks.put(id, task);
        return Optional.of(task);
    }

    public Optional<Task> updateTaskStatus(Long id, TaskStatus newStatus) {
        Task task = tasks.get(id);
        if (task == null) {
            return Optional.empty();
        }

        task.setStatus(newStatus);
        task.setUpdatedAt(LocalDateTime.now());

        tasks.put(id, task);
        return Optional.of(task);
    }

    public boolean deleteTaskById(Long id) {
        return tasks.remove(id) != null;
    }
}
