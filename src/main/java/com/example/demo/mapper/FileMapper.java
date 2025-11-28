package com.example.demo.mapper;

import com.example.demo.dto.FileUploadResponseDto;
import com.example.demo.model.File;

public class FileMapper {
    
    public static FileUploadResponseDto toDto(File file) {
        if (file == null) {
            return null;
        }
        
        return new FileUploadResponseDto(
            file.getId(),
            file.getFileName(),
            file.getOriginalFileName(),
            file.getFileType(),
            file.getSize(),
            file.getFilePath(),
            file.getUploadDate(),
            file.getUploadedBy() != null ? file.getUploadedBy().getUsername() : null
        );
    }
}