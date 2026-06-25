package com.projecthub.controller;

import com.projecthub.dto.SprintCreateRequest;
import com.projecthub.dto.SprintDto;
import com.projecthub.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Sprints", description = "Sprint management for projects")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping("/api/sprints")
    @Operation(summary = "List all sprints")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SprintDto>> getAll() {
        return ResponseEntity.ok(sprintService.findAll());
    }

    @GetMapping("/api/sprints/{id}")
    @Operation(summary = "Get sprint by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SprintDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.findById(id));
    }

    @GetMapping("/api/projects/{projectId}/sprints")
    @Operation(summary = "List sprints for a specific project")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SprintDto>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(sprintService.findByProjectId(projectId));
    }

    @PostMapping("/api/sprints")
    @Operation(summary = "Create a new sprint")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<SprintDto> create(@Valid @RequestBody SprintCreateRequest request) {
        SprintDto created = sprintService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/sprints/{id}")
    @Operation(summary = "Update a sprint")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<SprintDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody SprintCreateRequest request) {
        return ResponseEntity.ok(sprintService.update(id, request));
    }

    @PostMapping("/api/sprints/{id}/activate")
    @Operation(summary = "Activate a planned sprint")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<SprintDto> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.activate(id));
    }

    @PostMapping("/api/sprints/{id}/complete")
    @Operation(summary = "Complete an active sprint")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<SprintDto> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(sprintService.complete(id));
    }

    @DeleteMapping("/api/sprints/{id}")
    @Operation(summary = "Delete a sprint")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
