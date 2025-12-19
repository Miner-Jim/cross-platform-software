package com.example.demo.controller;

import com.example.demo.dto.ReportDto;
import com.example.demo.model.DeviceType;
import com.example.demo.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/devices/html")
    public String generateDevicesHtmlReport(
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Boolean active,
            Authentication authentication,
            Model model) {
        
        String username = authentication.getName();
        ReportDto report = reportService.generateDevicesReport(type, active, username);
        
        model.addAttribute("report", report);
        return "device-report";
    }
    
    @GetMapping("/devices/pdf")
    public ResponseEntity<byte[]> generateDevicesPdfReport(
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {
        
        String username = authentication.getName();
        byte[] pdfBytes = reportService.generateDevicesPdfReport(type, active, username);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"devices-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    
    @GetMapping("/summary/html")
    public String generateSummaryReport(Authentication authentication, Model model) {
        String username = authentication.getName();
        ReportDto report = reportService.generateSummaryReport(username);
        
        model.addAttribute("report", report);
        return "summary-report";
    }
}