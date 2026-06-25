package com.projecthub.service;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.TaskCreateRequest;
import com.projecthub.dto.TaskDto;
import com.projecthub.dto.TaskUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.Sprint;
import com.projecthub.entity.Task;
import com.projecthub.entity.User;
import com.projecthub.entity.enums.TaskStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.TaskMapper;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.SprintRepository;
import com.projecthub.repository.TaskRepository;
import com.projecthub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       SprintRepository sprintRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    public PageResponse<TaskDto> findAll(Pageable pageable) {
        Page<TaskDto> page = taskRepository.findAllByDeletedAtIsNull(pageable)
                .map(taskMapper::toDto);
        return PageResponse.from(page);
    }

    public TaskDto findById(UUID id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return taskMapper.toDto(task);
    }

    public List<TaskDto> findByProjectId(UUID projectId) {
        return taskMapper.toDtoList(taskRepository.findByProjectIdAndDeletedAtIsNull(projectId));
    }

    public PageResponse<TaskDto> findByProjectId(UUID projectId, Pageable pageable) {
        Page<TaskDto> page = taskRepository.findByProjectIdAndDeletedAtIsNull(projectId, pageable)
                .map(taskMapper::toDto);
        return PageResponse.from(page);
    }

    public List<TaskDto> findBySprintId(UUID sprintId) {
        return taskMapper.toDtoList(taskRepository.findBySprintIdAndDeletedAtIsNull(sprintId));
    }

    public List<TaskDto> findByStatus(TaskStatus status) {
        return taskMapper.toDtoList(taskRepository.findByStatusAndDeletedAtIsNull(status));
    }

    public List<TaskDto> findByAssigneeId(UUID assigneeId) {
        return taskMapper.toDtoList(taskRepository.findByAssigneeIdAndDeletedAtIsNull(assigneeId));
    }

    @Transactional
    public TaskDto create(TaskCreateRequest request) {
        if (taskRepository.existsByTaskNo(request.taskNo())) {
            throw new BusinessException("DUPLICATE_TASK_NO",
                    "Task with number '" + request.taskNo() + "' already exists");
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        Task task = taskMapper.toEntity(request);
        task.setProject(project);

        if (request.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.sprintId()));
            task.setSprint(sprint);
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            task.setAssignee(assignee);
        }

        if (request.reporterId() != null) {
            User reporter = userRepository.findById(request.reporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.reporterId()));
            task.setReporter(reporter);
        }

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.BACKLOG);
        }

        Task saved = taskRepository.save(task);
        log.info("Created task: {} - {}", saved.getTaskNo(), saved.getTitle());
        return taskMapper.toDto(saved);
    }

    @Transactional
    public TaskDto update(UUID id, TaskUpdateRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        taskMapper.updateEntity(request, task);

        if (request.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.sprintId()));
            task.setSprint(sprint);
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            task.setAssignee(assignee);
        }

        if (request.reporterId() != null) {
            User reporter = userRepository.findById(request.reporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.reporterId()));
            task.setReporter(reporter);
        }

        Task saved = taskRepository.save(task);
        log.info("Updated task: {}", saved.getId());
        return taskMapper.toDto(saved);
    }

    @Transactional
    public TaskDto updateStatus(UUID id, TaskStatus newStatus) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        TaskStatus currentStatus = task.getStatus();
        log.info("Transitioning task {} status: {} -> {}", id, currentStatus, newStatus);
        task.setStatus(newStatus);

        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.info("Soft deleted task: {}", id);
    }
}
