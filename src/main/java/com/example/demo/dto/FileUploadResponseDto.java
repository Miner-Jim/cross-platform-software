package com.example.demo.dto;

import java.time.LocalDateTime;

public record FileUploadResponseDto(
    Long id,
    String fileName,
    String originalFileName,
    String fileType,
    Long size,
    String filePath,
    LocalDateTime uploadDate,
    String uploadedBy
) {}