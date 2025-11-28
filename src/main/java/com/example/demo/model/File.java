package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalFileName;
    
    @Column(nullable = false)
    private String fileType; // MIME type
    
    @Column(nullable = false)
    private String fileExtension;
    
    @Column(nullable = false)
    private Long size;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private LocalDateTime uploadDate;
    
    @Column
    private String securityCheckStatus;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;
    
    public File(String fileName, String originalFileName, String fileType, 
                String fileExtension, Long size, String filePath, 
                LocalDateTime uploadDate, User uploadedBy) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileExtension = fileExtension;
        this.size = size;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
        this.securityCheckStatus = "CHECKED";
    }
}