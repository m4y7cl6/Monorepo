package com.projecthub.repository;

import com.projecthub.entity.Project;
import com.projecthub.entity.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByDeletedAtIsNull();

    Page<Project> findAllByDeletedAtIsNull(Pageable pageable);

    List<Project> findByStatusAndDeletedAtIsNull(ProjectStatus status);

    Optional<Project> findByCodeAndDeletedAtIsNull(String code);

    Optional<Project> findByIdAndDeletedAtIsNull(UUID id);

    long countByDeletedAtIsNull();

    @Query("SELECT COUNT(p) FROM Project p WHERE p.deletedAt IS NULL AND p.status = :status")
    long countByStatusAndDeletedAtIsNull(ProjectStatus status);

    boolean existsByCode(String code);
}
