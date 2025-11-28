package com.example.demo.repository;

import com.example.demo.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByUploadedByUsername(String username);
    Optional<File> findByFileName(String fileName);
    List<File> findByFileTypeContainingIgnoreCase(String fileType);
}