package com.projecthub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentDto(
        UUID id,
        UUID projectId,
        String projectName,
        String folderName,
        String fileName,
        String filePath,
        Long fileSize,
        String mimeType,
        UUID uploadedById,
        String uploadedByName,
        LocalDateTime uploadedAt
) {}
