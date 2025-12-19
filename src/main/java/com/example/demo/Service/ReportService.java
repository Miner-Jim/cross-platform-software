package com.example.demo.service;

import com.example.demo.dto.ReportDto;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final DeviceService deviceService;
    private final DeviceControlService deviceControlService;
    private final TemplateEngine templateEngine;
    
    public ReportDto generateDevicesReport(DeviceType type, Boolean active, String username) {
        log.info("Generating devices report for user: {}", username);
        
        List<Device> devices = deviceService.getAllDevices();
        
        if (type != null) {
            devices = devices.stream()
                    .filter(d -> d.getType() == type)
                    .collect(Collectors.toList());
        }
        
        if (active != null) {
            devices = devices.stream()
                    .filter(d -> d.isActive() == active)
                    .collect(Collectors.toList());
        }
        
        //Подготовка данных
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDevices", devices.size());
        summary.put("activeDevices", devices.stream().filter(Device::isActive).count());
        summary.put("totalPower", devices.stream().mapToDouble(Device::getPower).sum());
        summary.put("activePower", devices.stream()
                .filter(Device::isActive)
                .mapToDouble(Device::getPower)
                .sum());
        
        List<Map<String, Object>> details = devices.stream()
                .map(device -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", device.getId());
                    detail.put("title", device.getTitle());
                    detail.put("type", device.getType());
                    detail.put("power", device.getPower());
                    detail.put("active", device.isActive());
                    detail.put("room", device.getRoom() != null ? device.getRoom().getLocation() : "Не назначена");
                    return detail;
                })
                .collect(Collectors.toList());
        
        return new ReportDto(
            "Отчет по устройствам умного дома",
            LocalDateTime.now(),
            username,
            summary,
            details,
            "DEVICES"
        );
    }
    
    public ReportDto generateSummaryReport(String username) {
        log.info("Generating summary report for user: {}", username);
        
        List<Device> allDevices = deviceService.getAllDevices();
        double totalPower = deviceControlService.getTotalPowerConsumption();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDevices", allDevices.size());
        summary.put("activeDevices", allDevices.stream().filter(Device::isActive).count());
        summary.put("totalPowerConsumption", totalPower);
        summary.put("averagePower", allDevices.stream()
                .mapToDouble(Device::getPower)
                .average()
                .orElse(0.0));
        
        //по типам
        Map<DeviceType, Long> typeStats = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getType, Collectors.counting()));
        summary.put("devicesByType", typeStats);
        
        //по комнатам
        Map<String, Long> roomStats = allDevices.stream()
                .filter(d -> d.getRoom() != null)
                .collect(Collectors.groupingBy(d -> d.getRoom().getLocation(), Collectors.counting()));
        summary.put("devicesByRoom", roomStats);
        
        return new ReportDto(
            "Сводный отчет по умному дому",
            LocalDateTime.now(),
            username,
            summary,
            List.of(),
            "SUMMARY"
        );
    }
    
    public byte[] generateDevicesPdfReport(DeviceType type, Boolean active, String username) {
        try {
            ReportDto report = generateDevicesReport(type, active, username);
            
            Context context = new Context();
            context.setVariable("report", report);
            
            //Рендерим HTML
            String htmlContent = templateEngine.process("pdf/device-report", context);
            
            //Конвертируем HTML в PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            renderer.createPDF(baos);
            renderer.finishPDF();
            
            log.info("PDF report generated successfully for user: {}", username);
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }
}