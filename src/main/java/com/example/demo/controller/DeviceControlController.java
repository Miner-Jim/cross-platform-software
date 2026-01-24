package com.example.demo.controller;

import com.example.demo.service.DeviceControlService;
import com.example.demo.service.TemperatureService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.dto.DeviceResponseDto;
import com.example.demo.dto.DeviceToggleDto;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/control")
@Tag(name = "Управление")
public class DeviceControlController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceControlController.class);
    private final DeviceControlService deviceControlService;
    private final TemperatureService temperatureService;

    public DeviceControlController(DeviceControlService deviceControlService, 
                                 TemperatureService temperatureService) {
        this.deviceControlService = deviceControlService;
        this.temperatureService = temperatureService;
    }
      @Operation(summary = "Включить/выключить устройство")
      @PostMapping("/devices/{deviceId}/toggle")
    public ResponseEntity<DeviceResponseDto> toggleDevice(@PathVariable Long deviceId, 
                                             @RequestBody DeviceToggleDto request) {
        Boolean active = request.active();
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Device device = deviceControlService.toggleDevice(deviceId, active);
        if (device != null) {
            return ResponseEntity.ok(DeviceMapper.toDto(device));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Управление по температуре
    @Operation(summary = "Управление по температуре")
    @PostMapping("/temperature")
    public ResponseEntity<String> controlByTemperature(@RequestBody Map<String, Object> request) {
        logger.debug("POST /api/control/temperature");
        Long roomId = Long.valueOf(request.get("roomId").toString());
        Double temperature = Double.valueOf(request.get("temperature").toString());
        
        String result = temperatureService.controlByTemperature(roomId, temperature);
        return ResponseEntity.ok(result);
    }
    @Operation(summary = "Получить суммарное потребление")
    @GetMapping("/power")
    public ResponseEntity<Map<String, Double>> getTotalPower() {
        logger.debug("GET /api/control/power");
        double totalPower = deviceControlService.getTotalPowerConsumption();
        return ResponseEntity.ok(Map.of("totalPower", totalPower));
    }
    @Operation(summary = "Управление по типу устройства")
    @PostMapping("/type/{type}")
    public ResponseEntity<List<DeviceResponseDto>> toggleDevicesByType(@PathVariable DeviceType type,
                                                          @RequestBody DeviceToggleDto request) {
        Boolean active = request.active();
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Device> devices = deviceControlService.toggleDevicesByType(type, active);
        List<DeviceResponseDto> deviceDtos = devices.stream()
                .map(DeviceMapper::toDto)
                .toList();
        return ResponseEntity.ok(deviceDtos);
    }
}
