package com.projecthub.controller;

import com.projecthub.dto.BugCreateRequest;
import com.projecthub.dto.BugDto;
import com.projecthub.dto.PageResponse;
import com.projecthub.entity.enums.BugStatus;
import com.projecthub.service.BugService;
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
import java.util.UUID;

@RestController
@Tag(name = "Bugs", description = "Bug tracking and management")
public class BugController {

    private final BugService bugService;

    public BugController(BugService bugService) {
        this.bugService = bugService;
    }

    @GetMapping("/api/bugs")
    @Operation(summary = "List all bugs with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<BugDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(bugService.findAll(pageable));
    }

    @GetMapping("/api/bugs/{id}")
    @Operation(summary = "Get bug by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BugDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(bugService.findById(id));
    }

    @GetMapping("/api/projects/{projectId}/bugs")
    @Operation(summary = "List bugs for a specific project")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BugDto>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(bugService.findByProjectId(projectId));
    }

    @PostMapping("/api/bugs")
    @Operation(summary = "Report a new bug")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BugDto> create(@Valid @RequestBody BugCreateRequest request) {
        BugDto created = bugService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/bugs/{id}")
    @Operation(summary = "Update bug details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BugDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody BugCreateRequest request) {
        return ResponseEntity.ok(bugService.update(id, request));
    }

    @PatchMapping("/api/bugs/{id}/status")
    @Operation(summary = "Update bug status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BugDto> updateStatus(
            @PathVariable UUID id,
            @Parameter(description = "New bug status") @RequestParam BugStatus status) {
        return ResponseEntity.ok(bugService.updateStatus(id, status));
    }

    @DeleteMapping("/api/bugs/{id}")
    @Operation(summary = "Soft delete a bug")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bugService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
