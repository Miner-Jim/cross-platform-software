package com.example.demo.dto;

public record CsvImportDto(
    String message,
    int importedCount,
    int errorCount,
    java.util.List<String> errors,
    boolean hasErrors
) {}