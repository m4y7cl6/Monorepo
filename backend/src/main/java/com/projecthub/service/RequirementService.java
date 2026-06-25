package com.projecthub.service;

import com.projecthub.dto.PageResponse;
import com.projecthub.dto.RequirementCreateRequest;
import com.projecthub.dto.RequirementDto;
import com.projecthub.entity.Project;
import com.projecthub.entity.Requirement;
import com.projecthub.entity.enums.RequirementStatus;
import com.projecthub.exception.BusinessException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.mapper.RequirementMapper;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.RequirementRepository;
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
public class RequirementService {

    private static final Logger log = LoggerFactory.getLogger(RequirementService.class);

    private final RequirementRepository requirementRepository;
    private final ProjectRepository projectRepository;
    private final RequirementMapper requirementMapper;

    public RequirementService(RequirementRepository requirementRepository,
                               ProjectRepository projectRepository,
                               RequirementMapper requirementMapper) {
        this.requirementRepository = requirementRepository;
        this.projectRepository = projectRepository;
        this.requirementMapper = requirementMapper;
    }

    public PageResponse<RequirementDto> findAll(Pageable pageable) {
        Page<RequirementDto> page = requirementRepository.findAllByDeletedAtIsNull(pageable)
                .map(requirementMapper::toDto);
        return PageResponse.from(page);
    }

    public RequirementDto findById(UUID id) {
        Requirement requirement = requirementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));
        return requirementMapper.toDto(requirement);
    }

    public List<RequirementDto> findByProjectId(UUID projectId) {
        return requirementMapper.toDtoList(
                requirementRepository.findByProjectIdAndDeletedAtIsNull(projectId));
    }

    @Transactional
    public RequirementDto create(RequirementCreateRequest request) {
        if (requirementRepository.existsByReqNo(request.reqNo())) {
            throw new BusinessException("DUPLICATE_REQ_NO",
                    "Requirement with number '" + request.reqNo() + "' already exists");
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(request.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.projectId()));

        Requirement requirement = requirementMapper.toEntity(request);
        requirement.setProject(project);
        requirement.setStatus(RequirementStatus.DRAFT);

        Requirement saved = requirementRepository.save(requirement);
        log.info("Created requirement: {} - {}", saved.getReqNo(), saved.getTitle());
        return requirementMapper.toDto(saved);
    }

    @Transactional
    public RequirementDto update(UUID id, RequirementCreateRequest request) {
        Requirement requirement = requirementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));

        requirement.setTitle(request.title());
        requirement.setDescription(request.description());
        if (request.priority() != null) {
            requirement.setPriority(request.priority());
        }

        Requirement saved = requirementRepository.save(requirement);
        log.info("Updated requirement: {}", saved.getId());
        return requirementMapper.toDto(saved);
    }

    @Transactional
    public RequirementDto updateStatus(UUID id, RequirementStatus newStatus) {
        Requirement requirement = requirementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));
        requirement.setStatus(newStatus);
        Requirement saved = requirementRepository.save(requirement);
        log.info("Updated requirement {} status to: {}", id, newStatus);
        return requirementMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Requirement requirement = requirementRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement", "id", id));
        requirement.setDeletedAt(LocalDateTime.now());
        requirementRepository.save(requirement);
        log.info("Soft deleted requirement: {}", id);
    }
}
