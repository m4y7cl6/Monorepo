package com.projecthub.controller;

import com.projecthub.dto.DocumentDto;
import com.projecthub.dto.PageResponse;
import com.projecthub.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document storage and retrieval via MinIO")
public class DocumentController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("uploadedAt", "createdAt", "updatedAt", "fileName", "fileSize", "contentType");

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    @Operation(summary = "List all documents with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<DocumentDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "uploadedAt";
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(safeSort).ascending()
                : Sort.by(safeSort).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(documentService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document metadata by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document to MinIO storage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentDto> upload(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Associated project ID")
            @RequestParam(required = false) UUID projectId,

            @Parameter(description = "Uploader user ID", required = true)
            @RequestParam UUID uploadedById,

            @Parameter(description = "Folder name for organizing documents")
            @RequestParam(required = false) String folderName) {

        DocumentDto uploaded = documentService.upload(file, projectId, uploadedById, folderName);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a document and remove from MinIO")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
