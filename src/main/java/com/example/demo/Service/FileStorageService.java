// === FileStorageService.java === 
package com.example.demo.service;

import com.example.demo.model.File;
import com.example.demo.model.User;
import com.example.demo.repository.FileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileRepository fileRepository;
    private final UserService userService;
    private final List<String> allowedMimeTypes;
    private final List<String> allowedExtensions;

    public FileStorageService(
            @Value("${file.allowed.mime-types}") String allowedMimeTypesConfig,
            @Value("${file.allowed.extensions}") String allowedExtensionsConfig,
            @Value("${file.upload-dir:uploads}") String uploadDir,
            FileRepository fileRepository,
            UserService userService) throws IOException {
        
        this.allowedMimeTypes = Arrays.asList(allowedMimeTypesConfig.split(","));
        this.allowedExtensions = Arrays.asList(allowedExtensionsConfig.split(","));
        
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileRepository = fileRepository;
        this.userService = userService;
        
        Files.createDirectories(this.fileStorageLocation);
        
        log.info("FileStorageService initialized. Upload dir: {}, Allowed MIME types: {}, Allowed extensions: {}", 
                this.fileStorageLocation, this.allowedMimeTypes, this.allowedExtensions);
    }

    public File saveFile(MultipartFile file, String username) throws IOException {
        log.debug("Saving file: {} with MIME type: {}", file.getOriginalFilename(), file.getContentType());
        
        validateFile(file);
        validateMimeType(file);
        validateFileExtension(file);
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = generateFileName(fileExtension);
        
        //Только после успешной валидации сохраняем на диск
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        User uploadedBy = userService.getUserByUsername(username);
        
        File fileEntity = new File();
        fileEntity.setFileName(fileName);
        fileEntity.setOriginalFileName(originalFileName);
        fileEntity.setFileType(file.getContentType());
        fileEntity.setSize(file.getSize());
        fileEntity.setFilePath(targetLocation.toString());
        fileEntity.setUploadDate(LocalDateTime.now());
        fileEntity.setUploadedBy(uploadedBy);
        fileEntity.setFileExtension(fileExtension.toLowerCase());
        fileEntity.setSecurityCheckStatus("CHECKED");
        
        File savedFile = fileRepository.save(fileEntity);
        log.info("File saved successfully: {} with type: {}", savedFile.getFileName(), savedFile.getFileType());
        
        return savedFile;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
        
        if (file.getOriginalFilename() == null) {
            throw new FileValidationException("File name cannot be null");
        }
        
        if (file.getOriginalFilename().contains("..")) {
            throw new FileValidationException("Invalid file name: path traversal detected");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileValidationException("File size exceeds maximum limit of 10MB");
        }
    }

    private void validateMimeType(MultipartFile file) {
        String mimeType = file.getContentType();
        
        if (mimeType == null) {
            throw new FileValidationException("Cannot detect file type");
        }
        
        if (!allowedMimeTypes.contains(mimeType.toLowerCase())) {
            throw new FileValidationException(
                String.format("File type '%s' not allowed. Allowed types: %s", 
                    mimeType, String.join(", ", allowedMimeTypes))
            );
        }
        
        log.debug("MIME type validation passed: {}", mimeType);
    }

    private void validateFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName).toLowerCase();
        
        if (extension.isEmpty()) {
            throw new FileValidationException("File extension is missing");
        }
        
        if (!allowedExtensions.contains(extension)) {
            throw new FileValidationException(
                String.format("File extension '.%s' not allowed. Allowed extensions: %s", 
                    extension, String.join(", ", allowedExtensions))
            );
        }
        
        validateMimeTypeAndExtensionConsistency(file.getContentType(), extension);
        log.debug("File extension validation passed: {}", extension);
    }

    private void validateMimeTypeAndExtensionConsistency(String mimeType, String extension) {
        boolean isConsistent = false;
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                isConsistent = "image/jpeg".equals(mimeType);
                break;
            case "png":
                isConsistent = "image/png".equals(mimeType);
                break;
            case "gif":
                isConsistent = "image/gif".equals(mimeType);
                break;
            case "pdf":
                isConsistent = "application/pdf".equals(mimeType);
                break;
            case "doc":
                isConsistent = "application/msword".equals(mimeType);
                break;
            case "docx":
                isConsistent = "application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType);
                break;
            case "txt":
                isConsistent = "text/plain".equals(mimeType);
                break;
            default:
                isConsistent = true;
        }
        
        if (!isConsistent) {
            throw new FileValidationException(
                String.format("MIME type '%s' does not match file extension '.%s'", mimeType, extension)
            );
        }
    }

    public static class FileValidationException extends RuntimeException {
        public FileValidationException(String message) {
            super(message);
        }
    }

    private String generateFileName(String extension) {
        return UUID.randomUUID().toString() + (extension != null && !extension.isEmpty() ? "." + extension : "");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    public byte[] loadFileAsBytes(Long fileId) throws IOException {
        File file = getFile(fileId);
        return Files.readAllBytes(Paths.get(file.getFilePath()));
    }

    public boolean deleteFile(Long fileId) {
        return fileRepository.findById(fileId)
                .map(file -> {
                    try {
                        Files.deleteIfExists(Paths.get(file.getFilePath()));
                        fileRepository.delete(file);
                        log.info("File deleted successfully: {}", file.getFileName());
                        return true;
                    } catch (IOException e) {
                        log.error("Error deleting physical file: {}", e.getMessage());
                        return false;
                    }
                })
                .orElse(false);
    }


    public String getContentType(Long fileId) {
        File file = getFile(fileId);
        return file.getFileType();
    }

    public Long getSize(Long fileId) {
        File file = getFile(fileId);
        return file.getSize();
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }
}