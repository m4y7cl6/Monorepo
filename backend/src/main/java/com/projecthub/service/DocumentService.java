package com.projecthub.service;

import com.projecthub.config.MinioConfig;
import com.projecthub.dto.DocumentDto;
import com.projecthub.dto.PageResponse;
import com.projecthub.entity.Document;
import com.projecthub.entity.Project;
import com.projecthub.entity.User;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.DocumentMapper;
import com.projecthub.repository.DocumentRepository;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.UserRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DocumentMapper documentMapper;
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public DocumentService(DocumentRepository documentRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository,
                           DocumentMapper documentMapper,
                           MinioClient minioClient,
                           MinioConfig minioConfig) {
        this.documentRepository = documentRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.documentMapper = documentMapper;
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
    }

    public PageResponse<DocumentDto> findAll(Pageable pageable) {
        Page<DocumentDto> page = documentRepository.findAllByDeletedAtIsNull(pageable)
                .map(documentMapper::toDto);
        return PageResponse.from(page);
    }

    public DocumentDto findById(UUID id) {
        Document document = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return documentMapper.toDto(document);
    }

    public List<DocumentDto> findByProjectId(UUID projectId) {
        return documentMapper.toDtoList(documentRepository.findByProjectIdAndDeletedAtIsNull(projectId));
    }

    @Transactional
    public DocumentDto upload(MultipartFile file, UUID projectId, UUID uploadedById, String folderName) {
        Project project = null;
        if (projectId != null) {
            project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        }

        User uploader = userRepository.findById(uploadedById)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploadedById));

        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String objectPath = buildObjectPath(folderName, uniqueFileName);

        try {
            ensureBucketExists();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectPath)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Uploaded file to MinIO: {}", objectPath);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
            throw new BusinessException("MINIO_UPLOAD_FAILED", "Failed to upload file: " + e.getMessage());
        }

        Document document = Document.builder()
                .project(project)
                .folderName(folderName)
                .fileName(file.getOriginalFilename())
                .filePath(objectPath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploadedBy(uploader)
                .build();

        Document saved = documentRepository.save(document);
        log.info("Saved document record: {}", saved.getId());
        return documentMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Document document = documentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(document.getFilePath())
                            .build()
            );
            log.info("Removed file from MinIO: {}", document.getFilePath());
        } catch (Exception e) {
            log.warn("Failed to remove file from MinIO: {}. Proceeding with soft delete.", e.getMessage());
        }

        document.setDeletedAt(LocalDateTime.now());
        documentRepository.save(document);
        log.info("Soft deleted document: {}", id);
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
            log.info("Created MinIO bucket: {}", minioConfig.getBucketName());
        }
    }

    private String buildObjectPath(String folderName, String fileName) {
        if (folderName != null && !folderName.isBlank()) {
            return folderName.trim() + "/" + fileName;
        }
        return "uploads/" + fileName;
    }
}
