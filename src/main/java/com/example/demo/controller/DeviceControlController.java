package com.example.demo.controller;

import com.example.demo.service.DeviceControlService;
import com.example.demo.service.TemperatureService;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/control")
public class DeviceControlController {
    private final DeviceControlService deviceControlService;
    private final TemperatureService temperatureService;

    public DeviceControlController(DeviceControlService deviceControlService, 
                                 TemperatureService temperatureService) {
        this.deviceControlService = deviceControlService;
        this.temperatureService = temperatureService;
    }

    @PostMapping("/devices/{deviceId}/toggle")
    public ResponseEntity<Device> toggleDevice(@PathVariable Long deviceId, 
                                             @RequestBody Map<String, Boolean> request) {
        Boolean active = request.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Device device = deviceControlService.toggleDevice(deviceId, active);
        if (device != null) {
            return ResponseEntity.ok(device);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Управление по температуре
    @PostMapping("/temperature")
    public ResponseEntity<String> controlByTemperature(@RequestBody Map<String, Object> request) {
        Long roomId = Long.valueOf(request.get("roomId").toString());
        Double temperature = Double.valueOf(request.get("temperature").toString());
        
        String result = temperatureService.controlByTemperature(roomId, temperature);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/power")
    public ResponseEntity<Map<String, Double>> getTotalPower() {
        double totalPower = deviceControlService.getTotalPowerConsumption();
        return ResponseEntity.ok(Map.of("totalPower", totalPower));
    }

    @PostMapping("/type/{type}")
    public ResponseEntity<List<Device>> toggleDevicesByType(@PathVariable DeviceType type,
                                                          @RequestBody Map<String, Boolean> request) {
        Boolean active = request.get("active");
        if (active == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Device> devices = deviceControlService.toggleDevicesByType(type, active);
        return ResponseEntity.ok(devices);
    }
}
