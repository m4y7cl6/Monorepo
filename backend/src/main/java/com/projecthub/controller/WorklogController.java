package com.projecthub.controller;

import com.projecthub.dto.WorklogCreateRequest;
import com.projecthub.dto.WorklogDto;
import com.projecthub.entity.User;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.repository.UserRepository;
import com.projecthub.service.WorklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/worklogs")
@Tag(name = "Worklogs", description = "Time tracking and work log entries")
public class WorklogController {

    private final WorklogService worklogService;
    private final UserRepository userRepository;

    public WorklogController(WorklogService worklogService, UserRepository userRepository) {
        this.worklogService = worklogService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "List all worklogs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorklogDto>> getAll() {
        return ResponseEntity.ok(worklogService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get worklog by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorklogDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(worklogService.findById(id));
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "List worklogs for a specific task")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorklogDto>> getByTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(worklogService.findByTaskId(taskId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List worklogs for a specific user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorklogDto>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(worklogService.findByUserId(userId));
    }

    @GetMapping("/range")
    @Operation(summary = "List worklogs by date range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WorklogDto>> getByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(worklogService.findByDateRange(startDate, endDate));
    }

    @PostMapping
    @Operation(summary = "Create a new worklog entry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorklogDto> create(
            @Valid @RequestBody WorklogCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID resolvedUserId = resolveUserId(request.userId(), jwt);
        WorklogCreateRequest resolved = new WorklogCreateRequest(
                request.taskId(), resolvedUserId, request.workDate(),
                request.hours(), request.description());
        WorklogDto created = worklogService.create(resolved);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    private UUID resolveUserId(UUID requestUserId, Jwt jwt) {
        if (requestUserId != null) {
            return requestUserId;
        }
        String username = jwt.getClaimAsString("preferred_username");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return user.getId();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a worklog entry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WorklogDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorklogCreateRequest request) {
        return ResponseEntity.ok(worklogService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a worklog entry")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        worklogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
