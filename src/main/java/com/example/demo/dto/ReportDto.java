package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ReportDto(
    String title,
    LocalDateTime generatedAt,
    String generatedBy,
    Map<String, Object> summary,
    List<Map<String, Object>> details,
    String reportType
) {}