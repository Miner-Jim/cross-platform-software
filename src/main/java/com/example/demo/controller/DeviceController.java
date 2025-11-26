package com.example.demo.controller;

import com.example.demo.dto.DeviceRequestDto;
import com.example.demo.dto.DeviceResponseDto;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.service.DeviceService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/devices") // Все URL начинаются с /api/devices
public class DeviceController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private final DeviceService deviceService;
    private final UserService userService;
    private final DeviceRepository deviceRepository;
     private final RoomService roomService;
    
    public DeviceController(DeviceService deviceService, UserService userService, 
    DeviceRepository deviceRepository, RoomService roomService) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.deviceRepository = deviceRepository;
        this.roomService = roomService;
    }
    
     // GET /api/devices - получить все устройства
    @GetMapping
    public ResponseEntity<Page<DeviceResponseDto>> getAllDevices(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Double minPower,
            @RequestParam(required = false) Double maxPower, 
            @RequestParam(required = false) Boolean active,
            Authentication authentication,
            @PageableDefault(page = 0, size = 3, sort = "title") Pageable pageable) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        Page<Device> devices;
        
        if (user.getRole().getName().equals("USER")) {
            devices = deviceService.getDevicesByUserRoomsWithFilter(
                user.getId(), title, type, minPower, maxPower, active, pageable);
        } else {
            devices = deviceRepository.findAll(pageable);
        }
        
        // Конвертируем в DTO
        Page<DeviceResponseDto> deviceDtos = devices.map(DeviceMapper::toDto);
        return ResponseEntity.ok(deviceDtos);
    }
    
    // GET /api/devices/{id} - получить устройство по ID
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponseDto> getDeviceById(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        if (device != null) {
            return ResponseEntity.ok(DeviceMapper.toDto(device));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
   // POST /api/devices - создать новое устройство
    @PostMapping
    public ResponseEntity<DeviceResponseDto> createDevice(@RequestBody DeviceRequestDto deviceRequest) {
        logger.debug("POST /api/devices - creating device: {}", deviceRequest);
        try {
            // Конвертируем DTO в Entity
            Device device = new Device();
            device.setTitle(deviceRequest.title());
            device.setType(deviceRequest.type());
            device.setPower(deviceRequest.power());
            device.setActive(deviceRequest.active());
            
            // Устанавливаем комнату, если указана
            if (deviceRequest.roomId() != null) {
                Room room = roomService.getRoomById(deviceRequest.roomId());
                if (room == null) {
                    logger.warn("Room with id {} not found", deviceRequest.roomId());
                    return ResponseEntity.badRequest().build();
                }
                device.setRoom(room);
            }
            
            Device createdDevice = deviceService.createDevice(device);
            return ResponseEntity.status(HttpStatus.CREATED).body(DeviceMapper.toDto(createdDevice));
            
        } catch (Exception e) {
            logger.error("Error creating device: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // PUT /api/devices/{id} - обновить устройство
    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponseDto> updateDevice(@PathVariable Long id, @RequestBody DeviceRequestDto deviceRequest) {
        logger.debug("PUT /api/devices/{} - updating device: {}", id, deviceRequest);
        
        try {
            Device deviceDetails = new Device();
            deviceDetails.setTitle(deviceRequest.title());
            deviceDetails.setType(deviceRequest.type());
            deviceDetails.setPower(deviceRequest.power());
            deviceDetails.setActive(deviceRequest.active());
            
            // Обновляем комнату, если указана
            if (deviceRequest.roomId() != null) {
                Room room = roomService.getRoomById(deviceRequest.roomId());
                if (room == null) {
                    logger.warn("Room with id {} not found", deviceRequest.roomId());
                    return ResponseEntity.badRequest().build();
                }
                deviceDetails.setRoom(room);
            }
            
            Device updatedDevice = deviceService.updateDevice(id, deviceDetails);
            if (updatedDevice != null) {
                return ResponseEntity.ok(DeviceMapper.toDto(updatedDevice));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error updating device: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // DELETE /api/devices/{id} - удалить устройство
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        logger.debug("DELETE /api/devices/{}", id);
        boolean deleted = deviceService.deleteDevice(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Device ID {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
}
