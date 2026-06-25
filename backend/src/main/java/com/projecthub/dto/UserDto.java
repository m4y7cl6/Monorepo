package com.projecthub.dto;

import com.projecthub.entity.enums.UserRole;
import com.projecthub.entity.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        String displayName,
        UserRole role,
        UserStatus status,
        String keycloakId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
