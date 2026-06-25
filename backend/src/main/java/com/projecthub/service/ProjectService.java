package com.projecthub.service;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.enums.ProjectStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.ProjectMapper;
import com.projecthub.repository.ProjectRepository;
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
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public PageResponse<ProjectDto> findAll(Pageable pageable) {
        Page<ProjectDto> page = projectRepository.findAllByDeletedAtIsNull(pageable)
                .map(projectMapper::toDto);
        return PageResponse.from(page);
    }

    public List<ProjectDto> findAll() {
        return projectMapper.toDtoList(projectRepository.findAllByDeletedAtIsNull());
    }

    public ProjectDto findById(UUID id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return projectMapper.toDto(project);
    }

    public ProjectDto findByCode(String code) {
        Project project = projectRepository.findByCodeAndDeletedAtIsNull(code)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "code", code));
        return projectMapper.toDto(project);
    }

    public List<ProjectDto> findByStatus(ProjectStatus status) {
        return projectMapper.toDtoList(projectRepository.findByStatusAndDeletedAtIsNull(status));
    }

    @Transactional
    public ProjectDto create(ProjectCreateRequest request) {
        if (projectRepository.existsByCode(request.code())) {
            throw new BusinessException("DUPLICATE_CODE",
                    "Project with code '" + request.code() + "' already exists");
        }

        Project project = projectMapper.toEntity(request);
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.PLANNING);
        }

        Project saved = projectRepository.save(project);
        log.info("Created project: {} ({})", saved.getName(), saved.getCode());
        return projectMapper.toDto(saved);
    }

    @Transactional
    public ProjectDto update(UUID id, ProjectUpdateRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        projectMapper.updateEntity(request, project);
        Project saved = projectRepository.save(project);
        log.info("Updated project: {}", saved.getId());
        return projectMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        project.setDeletedAt(LocalDateTime.now());
        project.setStatus(ProjectStatus.CLOSED);
        projectRepository.save(project);
        log.info("Soft deleted project: {}", id);
    }
}
