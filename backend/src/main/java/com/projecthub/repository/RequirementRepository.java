package com.projecthub.repository;

import com.projecthub.entity.Requirement;
import com.projecthub.entity.enums.RequirementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequirementRepository extends JpaRepository<Requirement, UUID> {

    List<Requirement> findByProjectIdAndDeletedAtIsNull(UUID projectId);

    Page<Requirement> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);

    Page<Requirement> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Requirement> findByIdAndDeletedAtIsNull(UUID id);

    List<Requirement> findByProjectIdAndStatusAndDeletedAtIsNull(UUID projectId, RequirementStatus status);

    long countByProjectIdAndDeletedAtIsNull(UUID projectId);

    boolean existsByReqNo(String reqNo);
}
