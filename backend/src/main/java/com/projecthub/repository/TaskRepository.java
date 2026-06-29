package com.projecthub.repository;

import com.projecthub.entity.Task;
import com.projecthub.entity.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectIdAndDeletedAtIsNull(UUID projectId);

    Page<Task> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);

    List<Task> findBySprintIdAndDeletedAtIsNull(UUID sprintId);

    Page<Task> findBySprintIdAndDeletedAtIsNull(UUID sprintId, Pageable pageable);

    List<Task> findByStatusAndDeletedAtIsNull(TaskStatus status);

    Page<Task> findAllByDeletedAtIsNull(Pageable pageable);

    List<Task> findByAssigneeIdAndDeletedAtIsNull(UUID assigneeId);

    Page<Task> findByAssigneeIdAndDeletedAtIsNull(UUID assigneeId, Pageable pageable);

    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    long countByDeletedAtIsNull();

    long countByStatus(TaskStatus status);

    long countByProjectIdAndStatus(UUID projectId, TaskStatus status);

    long countByProjectIdAndDeletedAtIsNull(UUID projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.deletedAt IS NULL AND t.status = :status")
    long countByStatusAndDeletedAtIsNull(@Param("status") TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.sprint IS NULL AND t.deletedAt IS NULL")
    List<Task> findBacklogTasksByProjectId(@Param("projectId") UUID projectId);

    boolean existsByTaskNo(String taskNo);
}
