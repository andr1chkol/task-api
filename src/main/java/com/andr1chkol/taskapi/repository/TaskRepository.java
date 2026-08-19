package com.andr1chkol.taskapi.repository;

import com.andr1chkol.taskapi.model.Task;
import com.andr1chkol.taskapi.model.TaskStatus;
import com.andr1chkol.taskapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findAllByOwner(User owner, Pageable pageable);

    Page<Task> findAllByOwnerAndStatus(
            User owner,
            TaskStatus status,
            Pageable pageable
    );

    Optional<Task> findByIdAndOwner(Long id, User owner);
}
