package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.service.DeviceService;
import com.example.demo.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices") // Все URL начинаются с /api/devices
public class DeviceController {
    
    private final DeviceService deviceService;
    private final UserService userService;
    private final DeviceRepository deviceRepository;
    
    public DeviceController(DeviceService deviceService, UserService userService, DeviceRepository deviceRepository) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.deviceRepository = deviceRepository;
    }
    
    // GET /api/devices - получить все устройства
    @GetMapping
    public ResponseEntity<Page<Device>> getAllDevices(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Double minPower,
            @RequestParam(required = false) Double maxPower, 
            @RequestParam(required = false) Boolean active,
            Authentication authentication, // ✅ Добавляем аутентификацию
            @PageableDefault(page = 0, size = 3, sort = "title") Pageable pageable) {
        
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        Page<Device> devices;
        
        //USER видит устройства только из своих комнат
        if (user.getRole().getName().equals("USER")) {
            devices = deviceService.getDevicesByUserRoomsWithFilter(
                user.getId(), title, type, minPower, maxPower, active, pageable);
        } else {
             devices = deviceRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(devices);
    }
    
    // GET /api/devices/{id} - получить устройство по ID
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        if (device != null) {
            return ResponseEntity.ok(device);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // POST /api/devices - создать новое устройство
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device createdDevice = deviceService.createDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
    }
    
    // PUT /api/devices/{id} - обновить устройство
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device deviceDetails) {
        Device updatedDevice = deviceService.updateDevice(id, deviceDetails);
        if (updatedDevice != null) {
            return ResponseEntity.ok(updatedDevice);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE /api/devices/{id} - удалить устройство
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        boolean deleted = deviceService.deleteDevice(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
