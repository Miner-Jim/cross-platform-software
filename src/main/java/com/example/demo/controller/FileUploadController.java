package com.example.demo.controller;

import com.example.demo.dto.FileUploadResponseDto;
import com.example.demo.mapper.FileMapper;
import com.example.demo.model.File;
import com.example.demo.service.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/files")
@Tag(name = "Загрузка файлов")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    @Operation(summary = "Загрузка файла")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.debug("POST /api/files - uploading file: {}", file.getOriginalFilename());
        
        try {
            String username = authentication.getName();
            File savedFile = fileStorageService.saveFile(file, username);
            
            FileUploadResponseDto response = FileMapper.toDto(savedFile);
            return ResponseEntity.ok(response);
            
        } catch (FileStorageService.FileValidationException e) {
            //Специфичные ошибки валидации
            log.warn("File validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "File validation failed",
                "message", e.getMessage()
            ));
        } catch (IOException e) {
            log.error("IO error uploading file: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "File upload failed",
                "message", "Internal server error"
            ));
        } catch (Exception e) {
            log.error("Unexpected error uploading file: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Upload failed",
                "message", e.getMessage()
            ));
        }
    }
    @Operation(summary = "Получить файл по ID")
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        log.debug("GET /api/files/{} - downloading file", fileId);
        
        try {
            File file = fileStorageService.getFile(fileId);
            byte[] fileContent = fileStorageService.loadFileAsBytes(fileId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            //Content-Type правильный
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(file.getFileType());
            } catch (Exception e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + encodeFilename(file.getOriginalFileName()) + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error downloading file: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error downloading file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Получить информацию о файле")
    @GetMapping("/{fileId}/info")
    public ResponseEntity<FileUploadResponseDto> getFileInfo(@PathVariable Long fileId) {
        log.debug("GET /api/files/{}/info - getting file info", fileId);
        
        try {
            File file = fileStorageService.getFile(fileId);
            FileUploadResponseDto response = FileMapper.toDto(file);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting file info: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FileUploadResponseDto>> getUserFiles(Authentication authentication) {
        log.debug("GET /api/files - getting user files");
        
        try {
            String username = authentication.getName();
            return ResponseEntity.ok(List.of());
            
        } catch (Exception e) {
            log.error("Error getting user files: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @Operation(summary = "Показать допустимые расширения")
    @GetMapping("/allowed-types")
    public ResponseEntity<Map<String, Object>> getAllowedFileTypes() {
        log.debug("GET /api/files/allowed-types - getting allowed file types");
        
        Map<String, Object> response = Map.of(
            "allowedMimeTypes", fileStorageService.getAllowedMimeTypes(),
            "allowedExtensions", fileStorageService.getAllowedExtensions(),
            "maxFileSize", "10MB"
        );
        
        return ResponseEntity.ok(response);
    }
    @Operation(summary = "Удалить файл по ID")
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long fileId, Authentication authentication) {
        log.debug("DELETE /api/files/{} - deleting file", fileId);
        
        try {
            File file = fileStorageService.getFile(fileId);
            String currentUsername = authentication.getName();
            
            if (!file.getUploadedBy().getUsername().equals(currentUsername)) {
                return ResponseEntity.status(403).build();
            }
            
            boolean deleted = fileStorageService.deleteFile(fileId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    @Operation(summary = "Получить тип файла")
    @GetMapping("/{fileId}/content-type")
    public ResponseEntity<String> getFileContentType(@PathVariable Long fileId) {
        log.debug("GET /api/files/{}/content-type", fileId);
        
        try {
            String contentType = fileStorageService.getContentType(fileId);
            return ResponseEntity.ok(contentType);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @Operation(summary = "Получить размер файла")
    @GetMapping("/{fileId}/size")
    public ResponseEntity<Long> getFileSize(@PathVariable Long fileId) {
        log.debug("GET /api/files/{}/size", fileId);
        
        try {
            Long size = fileStorageService.getSize(fileId);
            return ResponseEntity.ok(size);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String encodeFilename(String filename) {
    try {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8.toString()).replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
        return filename;
    }
    }
}