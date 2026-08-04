package com.andr1chkol.taskapi.repository;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
