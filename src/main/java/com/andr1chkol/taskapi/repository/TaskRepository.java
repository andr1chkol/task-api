package com.andr1chkol.taskapi.repository;

import com.andr1chkol.taskapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
