package com.projecthub.repository;

import com.projecthub.entity.Bug;
import com.projecthub.entity.enums.BugSeverity;
import com.projecthub.entity.enums.BugStatus;
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
public interface BugRepository extends JpaRepository<Bug, UUID> {

    List<Bug> findByProjectIdAndDeletedAtIsNull(UUID projectId);

    Page<Bug> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);

    Page<Bug> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Bug> findByIdAndDeletedAtIsNull(UUID id);

    long countByStatus(BugStatus status);

    long countBySeverityAndDeletedAtIsNull(BugSeverity severity);

    long countByProjectIdAndDeletedAtIsNull(UUID projectId);

    @Query("SELECT COUNT(b) FROM Bug b WHERE b.deletedAt IS NULL AND b.status = :status")
    long countByStatusAndDeletedAtIsNull(@Param("status") BugStatus status);

    List<Bug> findByAssigneeIdAndDeletedAtIsNull(UUID assigneeId);

    boolean existsByBugNo(String bugNo);
}
