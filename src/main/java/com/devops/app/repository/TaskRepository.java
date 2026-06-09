package com.devops.app.repository;

import com.devops.app.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    Page<Task> findByPriority(Task.Priority priority, Pageable pageable);

    Page<Task> findByStatusAndPriority(Task.TaskStatus status, Task.Priority priority, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
           "(:title IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:priority IS NULL OR t.priority = :priority)")
    Page<Task> search(
        @Param("title") String title,
        @Param("status") Task.TaskStatus status,
        @Param("priority") Task.Priority priority,
        Pageable pageable
    );

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();

    long countByStatus(Task.TaskStatus status);
}
