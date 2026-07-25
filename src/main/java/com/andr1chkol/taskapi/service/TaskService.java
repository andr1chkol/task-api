package com.andr1chkol.taskapi.service;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private final List<Task> tasks = new ArrayList<>();
    private long nextId = 2L;

    public TaskService() {
        Task task = new Task("Learn Spring Boot", "Create TaskApi web");
        task.setId(1L);

        tasks.add(task);
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public Task createTask(Task task) {
        task.setId(nextId++);
        task.setStatus(TaskStatus.TODO);

        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        tasks.add(task);
        return task;
    }
}
