package com.projecthub.service;

import com.projecthub.dto.SprintCreateRequest;
import com.projecthub.dto.SprintDto;
import com.projecthub.entity.Project;
import com.projecthub.entity.Sprint;
import com.projecthub.entity.enums.SprintStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.SprintMapper;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.SprintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;

    public SprintService(SprintRepository sprintRepository,
                         ProjectRepository projectRepository,
                         SprintMapper sprintMapper) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
        this.sprintMapper = sprintMapper;
    }

    public List<SprintDto> findAll() {
        return sprintMapper.toDtoList(sprintRepository.findAll());
    }

    public SprintDto findById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        return sprintMapper.toDto(sprint);
    }

    public List<SprintDto> findByProjectId(UUID projectId) {
        return sprintMapper.toDtoList(
                sprintRepository.findByProjectIdOrderByStartDateAsc(projectId));
    }

    public List<SprintDto> findByStatus(SprintStatus status) {
        return sprintMapper.toDtoList(sprintRepository.findByStatus(status));
    }

    @Transactional
    public SprintDto create(SprintCreateRequest request) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        Sprint sprint = sprintMapper.toEntity(request);
        sprint.setProject(project);
        if (sprint.getStatus() == null) {
            sprint.setStatus(SprintStatus.PLANNED);
        }

        Sprint saved = sprintRepository.save(sprint);
        log.info("Created sprint: {} for project: {}", saved.getSprintName(), project.getCode());
        return sprintMapper.toDto(saved);
    }

    @Transactional
    public SprintDto update(UUID id, SprintCreateRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        sprint.setSprintName(request.name());
        sprint.setStartDate(request.startDate());
        sprint.setEndDate(request.endDate());
        sprint.setGoal(request.goal());
        if (request.status() != null) {
            sprint.setStatus(request.status());
        }

        Sprint saved = sprintRepository.save(sprint);
        log.info("Updated sprint: {}", saved.getId());
        return sprintMapper.toDto(saved);
    }

    @Transactional
    public SprintDto activate(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.PLANNED) {
            throw new BusinessException("INVALID_SPRINT_STATE",
                    "Only PLANNED sprints can be activated. Current status: " + sprint.getStatus());
        }

        List<Sprint> activeSprints = sprintRepository.findByProjectIdAndStatus(
                sprint.getProject().getId(), SprintStatus.ACTIVE);
        if (!activeSprints.isEmpty()) {
            throw new BusinessException("ACTIVE_SPRINT_EXISTS",
                    "Project already has an active sprint. Complete it before activating another.");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        Sprint saved = sprintRepository.save(sprint);
        log.info("Activated sprint: {}", saved.getId());
        return sprintMapper.toDto(saved);
    }

    @Transactional
    public SprintDto complete(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BusinessException("INVALID_SPRINT_STATE",
                    "Only ACTIVE sprints can be completed. Current status: " + sprint.getStatus());
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        Sprint saved = sprintRepository.save(sprint);
        log.info("Completed sprint: {}", saved.getId());
        return sprintMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BusinessException("CANNOT_DELETE_ACTIVE_SPRINT",
                    "Active sprints cannot be deleted. Complete the sprint first.");
        }

        sprintRepository.delete(sprint);
        log.info("Deleted sprint: {}", id);
    }
}
