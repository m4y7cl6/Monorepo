package com.projecthub.controller;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.RequirementCreateRequest;
import com.projecthub.dto.RequirementDto;
import com.projecthub.entity.enums.RequirementStatus;
import com.projecthub.service.RequirementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@Tag(name = "Requirements", description = "Requirements management")
public class RequirementController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "reqNo", "title", "status", "priority");

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @GetMapping("/api/requirements")
    @Operation(summary = "List all requirements with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<RequirementDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(safeSort).ascending()
                : Sort.by(safeSort).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(requirementService.findAll(pageable));
    }

    @GetMapping("/api/requirements/{id}")
    @Operation(summary = "Get requirement by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RequirementDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(requirementService.findById(id));
    }

    @GetMapping("/api/projects/{projectId}/requirements")
    @Operation(summary = "List requirements for a specific project")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RequirementDto>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(requirementService.findByProjectId(projectId));
    }

    @PostMapping("/api/requirements")
    @Operation(summary = "Create a new requirement")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<RequirementDto> create(@Valid @RequestBody RequirementCreateRequest request) {
        RequirementDto created = requirementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/requirements/{id}")
    @Operation(summary = "Update a requirement")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<RequirementDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody RequirementCreateRequest request) {
        return ResponseEntity.ok(requirementService.update(id, request));
    }

    @PatchMapping("/api/requirements/{id}/status")
    @Operation(summary = "Update requirement status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<RequirementDto> updateStatus(
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, String> body) {
        RequirementStatus status = RequirementStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(requirementService.updateStatus(id, status));
    }

    @DeleteMapping("/api/requirements/{id}")
    @Operation(summary = "Soft delete a requirement")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        requirementService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
