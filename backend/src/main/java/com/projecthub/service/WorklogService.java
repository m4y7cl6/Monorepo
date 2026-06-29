package com.projecthub.service;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.WorklogCreateRequest;
import com.projecthub.dto.WorklogDto;
import com.projecthub.entity.Task;
import com.projecthub.entity.User;
import com.projecthub.entity.Worklog;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.WorklogMapper;
import com.projecthub.repository.TaskRepository;
import com.projecthub.repository.UserRepository;
import com.projecthub.repository.WorklogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorklogService {

    private static final Logger log = LoggerFactory.getLogger(WorklogService.class);

    private final WorklogRepository worklogRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorklogMapper worklogMapper;

    public WorklogService(WorklogRepository worklogRepository,
                          TaskRepository taskRepository,
                          UserRepository userRepository,
                          WorklogMapper worklogMapper) {
        this.worklogRepository = worklogRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.worklogMapper = worklogMapper;
    }

    public PageResponse<WorklogDto> findAll(Pageable pageable) {
        return PageResponse.from(worklogRepository.findAll(pageable).map(worklogMapper::toDto));
    }

    public List<WorklogDto> findAll() {
        return worklogMapper.toDtoList(worklogRepository.findAll());
    }

    public WorklogDto findById(UUID id) {
        Worklog worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worklog", "id", id));
        return worklogMapper.toDto(worklog);
    }

    public List<WorklogDto> findByTaskId(UUID taskId) {
        return worklogMapper.toDtoList(worklogRepository.findByTaskId(taskId));
    }

    public List<WorklogDto> findByUserId(UUID userId) {
        return worklogMapper.toDtoList(worklogRepository.findByUserId(userId));
    }

    public PageResponse<WorklogDto> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return PageResponse.from(
                worklogRepository.findByWorkDateBetween(startDate, endDate, pageable).map(worklogMapper::toDto));
    }

    public List<WorklogDto> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return worklogMapper.toDtoList(worklogRepository.findByWorkDateBetween(startDate, endDate));
    }

    public List<WorklogDto> findByUserIdAndDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return worklogMapper.toDtoList(
                worklogRepository.findByUserIdAndWorkDateBetween(userId, startDate, endDate));
    }

    @Transactional
    public WorklogDto create(WorklogCreateRequest request) {
        Task task = taskRepository.findByIdAndDeletedAtIsNull(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", request.taskId()));

        User user = userRepository.findById(request.userId())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        Worklog worklog = worklogMapper.toEntity(request);
        worklog.setTask(task);
        worklog.setUser(user);

        Worklog saved = worklogRepository.save(worklog);
        log.info("Created worklog: {} hours for task {} by user {}",
                saved.getHours(), task.getTaskNo(), user.getUsername());
        return worklogMapper.toDto(saved);
    }

    @Transactional
    public WorklogDto update(UUID id, WorklogCreateRequest request) {
        Worklog worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worklog", "id", id));

        worklog.setWorkDate(request.workDate());
        worklog.setHours(request.hours());
        worklog.setDescription(request.description());

        Worklog saved = worklogRepository.save(worklog);
        log.info("Updated worklog: {}", saved.getId());
        return worklogMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Worklog worklog = worklogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Worklog", "id", id));
        worklogRepository.delete(worklog);
        log.info("Deleted worklog: {}", id);
    }
}
