package com.projecthub.controller;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.TaskCreateRequest;
import com.projecthub.dto.TaskDto;
import com.projecthub.dto.TaskUpdateRequest;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.service.TaskService;
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
@Tag(name = "Tasks", description = "Task management and tracking")
public class TaskController {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "updatedAt", "taskNo", "title", "status", "priority", "dueDate");

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/api/tasks")
    @Operation(summary = "List all tasks with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<TaskDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        String safeSort = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(safeSort).ascending()
                : Sort.by(safeSort).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(taskService.findAll(pageable));
    }

    @GetMapping("/api/tasks/{id}")
    @Operation(summary = "Get task by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @GetMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "List tasks for a specific project")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<TaskDto>> getByProject(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(taskService.findByProjectId(projectId, pageable));
    }

    @GetMapping("/api/sprints/{sprintId}/tasks")
    @Operation(summary = "List tasks for a specific sprint")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskDto>> getBySprint(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(taskService.findBySprintId(sprintId));
    }

    @PostMapping("/api/tasks")
    @Operation(summary = "Create a new task")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER', 'DEVELOPER')")
    public ResponseEntity<TaskDto> create(@Valid @RequestBody TaskCreateRequest request) {
        TaskDto created = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/tasks/{id}")
    @Operation(summary = "Update a task")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER', 'DEVELOPER')")
    public ResponseEntity<TaskDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @PatchMapping("/api/tasks/{id}/status")
    @Operation(summary = "Update task status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskDto> updateStatus(
            @PathVariable UUID id,
            @RequestBody java.util.Map<String, String> body) {
        TaskStatus status = TaskStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(taskService.updateStatus(id, status));
    }

    @DeleteMapping("/api/tasks/{id}")
    @Operation(summary = "Soft delete a task")
    @PreAuthorize("hasAnyRole('ADMIN', 'PM', 'LEADER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
