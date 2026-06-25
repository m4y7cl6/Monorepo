package com.projecthub.repository;

import com.projecthub.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByProjectIdAndDeletedAtIsNull(UUID projectId);

    Page<Document> findByProjectIdAndDeletedAtIsNull(UUID projectId, Pageable pageable);

    Page<Document> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Document> findByIdAndDeletedAtIsNull(UUID id);

    List<Document> findByUploadedByIdAndDeletedAtIsNull(UUID userId);

    long countByProjectIdAndDeletedAtIsNull(UUID projectId);
}
