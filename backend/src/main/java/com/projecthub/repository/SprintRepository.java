package com.projecthub.repository;

import com.projecthub.entity.Sprint;
import com.projecthub.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    List<Sprint> findByProjectId(UUID projectId);

    List<Sprint> findByProjectIdOrderByStartDateAsc(UUID projectId);

    List<Sprint> findByStatus(SprintStatus status);

    List<Sprint> findByProjectIdAndStatus(UUID projectId, SprintStatus status);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = 'ACTIVE'")
    Optional<Sprint> findActiveSprintByProjectId(@Param("projectId") UUID projectId);

    long countByProjectId(UUID projectId);
}
