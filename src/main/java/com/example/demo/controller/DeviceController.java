package com.example.demo.controller;

import com.example.demo.dto.CsvImportDto;
import com.example.demo.dto.DeviceRequestDto;
import com.example.demo.dto.DeviceResponseDto;
import com.example.demo.mapper.DeviceMapper;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.service.CsvParserUtil;
import com.example.demo.service.DeviceService;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@RestController
@RequestMapping("/api/devices") // Все URL начинаются с /api/devices
@Tag(name = "Устройства")
public class DeviceController {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private final DeviceService deviceService;
    private final UserService userService;
    private final DeviceRepository deviceRepository;
    private final RoomService roomService;
    private final CsvParserUtil csvParserUtil;
    
    public DeviceController(DeviceService deviceService, UserService userService, 
    DeviceRepository deviceRepository, RoomService roomService, CsvParserUtil csvParserUtil) {
        this.deviceService = deviceService;
        this.userService = userService;
        this.deviceRepository = deviceRepository;
        this.roomService = roomService;
        this.csvParserUtil = csvParserUtil;
    }
    
     // GET /api/devices - получить все устройства
    @Operation(summary = "Получить все устройства")
    @GetMapping
    public ResponseEntity<Page<DeviceResponseDto>> getAllDevices(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DeviceType type,
            @RequestParam(required = false) Double minPower,
            @RequestParam(required = false) Double maxPower, 
            @RequestParam(required = false) Boolean active,
            Authentication authentication,
            @PageableDefault(page = 0, size = 3) Pageable pageable) {

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
    @Operation(summary = "Получить устройство по ID")
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
    @Operation(summary = "Создать устройство")
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
    @Operation(summary = "Обновить устройство")
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
    @Operation(summary = "Удалить устройство")
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
    @Operation(summary = "Импорт из CSV")
    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportDto> importDevicesFromCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.debug("POST /api/devices/import/csv - importing devices from CSV");
        
        // Проверяем что файл CSV
        if (!isCsvFile(file)) {
            return ResponseEntity.badRequest().body(new CsvImportDto(
                "File must be CSV format", 0, 0, List.of("Invalid file type"), true
            ));
        }

        try {
            // Парсим CSV
            CsvParserUtil.CsvParseResult<Device> parseResult = csvParserUtil.parseCsvFile(
                file, this::mapCsvRecordToDevice
            );

            // Сохраняем устройства
            List<Device> savedDevices = new ArrayList<>();
            List<String> saveErrors = new ArrayList<>(parseResult.getErrors());

            for (int i = 0; i < parseResult.getRecords().size(); i++) {
                try {
                    Device device = parseResult.getRecords().get(i);
                    Device savedDevice = deviceService.createDevice(device);
                    savedDevices.add(savedDevice);
                } catch (Exception e) {
                    String error = String.format("Device %d '%s': %s", 
                        i + 1, parseResult.getRecords().get(i).getTitle(), e.getMessage());
                    saveErrors.add(error);
                }
            }

            boolean hasErrors = !saveErrors.isEmpty() || parseResult.isHasErrors();
            
            if (hasErrors) {
                return ResponseEntity.ok(new CsvImportDto(
                    "Import completed with errors",
                    savedDevices.size(),
                    saveErrors.size(),
                    saveErrors,
                    true
                ));
            } else {
                return ResponseEntity.ok(new CsvImportDto(
                    "Import completed successfully",
                    savedDevices.size(),
                    0,
                    List.of(),
                    false
                ));
            }

        } catch (Exception e) {
            log.error("CSV import failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(new CsvImportDto(
                "Import failed: " + e.getMessage(), 0, 1, List.of(e.getMessage()), true
            ));
        }
    }

    private Device mapCsvRecordToDevice(CSVRecord record) {
        // Получаем значения из CSV
        String title = record.get("title");
        String typeStr = record.get("type");
        String powerStr = record.get("power");
        String activeStr = record.get("active");
        String roomLocation = record.get("roomLocation");

        // Валидация обязательных полей
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Type is required");
        }

        // Парсинг типа устройства
        DeviceType type;
        try {
            type = DeviceType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid device type: " + typeStr);
        }

        // Парсинг мощности
        double power = 0.0;
        if (powerStr != null && !powerStr.trim().isEmpty()) {
            try {
                power = Double.parseDouble(powerStr.trim());
                if (power < 0) {
                    throw new IllegalArgumentException("Power cannot be negative");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid power value: " + powerStr);
            }
        }

        // Парсинг статуса активности
        boolean active = false;
        if (activeStr != null && !activeStr.trim().isEmpty()) {
            String activeLower = activeStr.trim().toLowerCase();
            if ("true".equals(activeLower) || "1".equals(activeLower) || "yes".equals(activeLower)) {
                active = true;
            } else if (!"false".equals(activeLower) && !"0".equals(activeLower) && !"no".equals(activeLower)) {
                throw new IllegalArgumentException("Invalid active value: " + activeStr);
            }
        }

        // Создаем устройство
        Device device = new Device();
        device.setTitle(title.trim());
        device.setType(type);
        device.setPower(power);
        device.setActive(active);

        // Привязываем к комнате если указана локация
        if (roomLocation != null && !roomLocation.trim().isEmpty()) {
            List<Room> rooms = roomService.getRoomByLocation(roomLocation.trim());
            if (!rooms.isEmpty()) {
                device.setRoom(rooms.get(0));
            } else {
                throw new IllegalArgumentException("Room not found with location: " + roomLocation);
            }
        }

        return device;
    }

    private boolean isCsvFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return "text/csv".equals(contentType) || 
            (fileName != null && fileName.toLowerCase().endsWith(".csv"));
    }

}
