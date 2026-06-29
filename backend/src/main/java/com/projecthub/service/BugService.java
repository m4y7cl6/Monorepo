package com.projecthub.service;

import com.projecthub.dto.BugCreateRequest;
import com.projecthub.dto.BugDto;
import com.projecthub.dto.PageResponse;
import com.projecthub.entity.Bug;
import com.projecthub.entity.Project;
import com.projecthub.entity.User;
import com.projecthub.entity.enums.BugStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.BugMapper;
import com.projecthub.repository.BugRepository;
import com.projecthub.repository.ProjectRepository;
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
public class BugService {

    private static final Logger log = LoggerFactory.getLogger(BugService.class);

    private final BugRepository bugRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final BugMapper bugMapper;

    public BugService(BugRepository bugRepository,
                      ProjectRepository projectRepository,
                      UserRepository userRepository,
                      BugMapper bugMapper) {
        this.bugRepository = bugRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.bugMapper = bugMapper;
    }

    public PageResponse<BugDto> findAll(Pageable pageable) {
        Page<BugDto> page = bugRepository.findAllByDeletedAtIsNull(pageable)
                .map(bugMapper::toDto);
        return PageResponse.from(page);
    }

    public BugDto findById(UUID id) {
        Bug bug = bugRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug", "id", id));
        return bugMapper.toDto(bug);
    }

    public List<BugDto> findByProjectId(UUID projectId) {
        return bugMapper.toDtoList(bugRepository.findByProjectIdAndDeletedAtIsNull(projectId));
    }

    @Transactional
    public BugDto create(BugCreateRequest request) {
        if (bugRepository.existsByBugNo(request.bugNo())) {
            throw new BusinessException("DUPLICATE_BUG_NO",
                    "Bug with number '" + request.bugNo() + "' already exists");
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        Bug bug = bugMapper.toEntity(request);
        bug.setProject(project);
        bug.setStatus(BugStatus.NEW);

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            bug.setAssignee(assignee);
        }

        if (request.reporterId() != null) {
            User reporter = userRepository.findById(request.reporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.reporterId()));
            bug.setReporter(reporter);
        }

        Bug saved = bugRepository.save(bug);
        log.info("Created bug: {} - {}", saved.getBugNo(), saved.getTitle());
        return bugMapper.toDto(saved);
    }

    @Transactional
    public BugDto updateStatus(UUID id, BugStatus newStatus) {
        Bug bug = bugRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug", "id", id));
        bug.setStatus(newStatus);
        Bug saved = bugRepository.save(bug);
        log.info("Updated bug {} status to: {}", id, newStatus);
        return bugMapper.toDto(saved);
    }

    @Transactional
    public BugDto update(UUID id, BugCreateRequest request) {
        Bug bug = bugRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug", "id", id));

        bug.setTitle(request.title());
        bug.setDescription(request.description());
        if (request.severity() != null) {
            bug.setSeverity(request.severity());
        }
        if (request.priority() != null) {
            bug.setPriority(request.priority());
        }
        if (request.status() != null) {
            bug.setStatus(request.status());
        }

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.assigneeId()));
            bug.setAssignee(assignee);
        }

        if (request.reporterId() != null) {
            User reporter = userRepository.findById(request.reporterId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.reporterId()));
            bug.setReporter(reporter);
        }

        Bug saved = bugRepository.save(bug);
        log.info("Updated bug: {}", saved.getId());
        return bugMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Bug bug = bugRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bug", "id", id));
        bug.setDeletedAt(LocalDateTime.now());
        bugRepository.save(bug);
        log.info("Soft deleted bug: {}", id);
    }
}
