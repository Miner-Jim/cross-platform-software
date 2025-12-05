package com.example.demo.controller;

import com.example.demo.dto.RoomDto;
import com.example.demo.dto.CsvImportDto;
import com.example.demo.dto.RoomCreateDto;
import com.example.demo.mapper.RoomMapper;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.service.CsvParserUtil;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final UserService userService;
    private final CsvParserUtil csvParserUtil;

    public RoomController(RoomService roomService, UserService userService, CsvParserUtil csvParserUtil)  {
        this.roomService = roomService;
        this.userService = userService;
        this.csvParserUtil = csvParserUtil;
    }

    @GetMapping
    public List<RoomDto> getAllRooms(Authentication authentication) {
        logger.debug("GET /api/rooms"); 
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        List<Room> rooms;
        if (user.getRole().getName().equals("USER")) {
            logger.debug("GET /api/rooms - USER");
            rooms = roomService.getRoomsByManager(user.getId());
        } else {
            logger.debug("GET /api/rooms - ADMIN");
            rooms = roomService.getAllRooms();
        }
        
        return rooms.stream()
                .map(RoomMapper::toDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id, Authentication authentication) {
        logger.debug("GET /api/rooms/{}", id);
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        
        Room room = roomService.getRoomById(id);
        if (room == null) {
            logger.warn("Room with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
        
        // ✅ USER может смотреть только свои комнаты
        if (user.getRole().getName().equals("USER") && 
            !roomService.isRoomManager(id, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(RoomMapper.toDto(room));
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@RequestBody RoomCreateDto roomCreateDto) {
        logger.debug("POST /api/rooms - {}", roomCreateDto);
        
        // Проверяем, что указан менеджер
        if (roomCreateDto.managerId() == null) {
            logger.warn("Manager ID is not specified");
            return ResponseEntity.badRequest().build();
        }
        
        // Загружаем полный объект менеджера из базы
        User manager = userService.getUserById(roomCreateDto.managerId());
        if (manager == null) {
            logger.warn("Manager with id {} not found", roomCreateDto.managerId());
            return ResponseEntity.badRequest().build();
        }
        
        // Создаем Room из DTO
        Room room = new Room();
        room.setLocation(roomCreateDto.location());
        room.setManager(manager);
        
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomMapper.toDto(createdRoom));
    }
    
    // PUT /api/rooms/{id} - обновить комнату
    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @RequestBody RoomCreateDto roomCreateDto) {
        logger.debug("PUT /api/rooms/{} - {}", id, roomCreateDto);
        
        Room roomDetails = new Room();
        roomDetails.setLocation(roomCreateDto.location());
        
        // Если в запросе указан новый менеджер, проверяем его существование
        if (roomCreateDto.managerId() != null) {
            User manager = userService.getUserById(roomCreateDto.managerId());
            if (manager == null) {
                logger.warn("Manager with id {} not found", roomCreateDto.managerId());
                return ResponseEntity.badRequest().build();
            }
            roomDetails.setManager(manager);
        }
        
        Room updatedRoom = roomService.updateRoom(id, roomDetails);
        if (updatedRoom != null) {
            return ResponseEntity.ok(RoomMapper.toDto(updatedRoom));
        } else {
            logger.warn("Room with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
    
    // DELETE /api/rooms/{id} - удалить комнату
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        logger.debug("DELETE /api/rooms/{}", id);
        boolean deleted = roomService.deleteRoom(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Room with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CsvImportDto> importRoomsFromCsv(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.debug("POST /api/rooms/import/csv - importing rooms from CSV");
        
        if (!isCsvFile(file)) {
            return ResponseEntity.badRequest().body(new CsvImportDto(
                "File must be CSV format", 0, 0, List.of("Invalid file type"), true
            ));
        }

        try {
            String username = authentication.getName();
            User currentUser = userService.getUserByUsername(username);

            CsvParserUtil.CsvParseResult<Room> parseResult = csvParserUtil.parseCsvFile(
                file, record -> mapCsvRecordToRoom(record, currentUser)
            );

            List<Room> savedRooms = new ArrayList<>();
            List<String> saveErrors = new ArrayList<>(parseResult.getErrors());

            for (int i = 0; i < parseResult.getRecords().size(); i++) {
                try {
                    Room room = parseResult.getRecords().get(i);
                    Room savedRoom = roomService.createRoom(room);
                    savedRooms.add(savedRoom);
                } catch (Exception e) {
                    String error = String.format("Room %d '%s': %s", 
                        i + 1, parseResult.getRecords().get(i).getLocation(), e.getMessage());
                    saveErrors.add(error);
                }
            }

            boolean hasErrors = !saveErrors.isEmpty() || parseResult.isHasErrors();
            
            return ResponseEntity.ok(new CsvImportDto(
                hasErrors ? "Import completed with errors" : "Import completed successfully",
                savedRooms.size(),
                saveErrors.size(),
                saveErrors,
                hasErrors
            ));

        } catch (Exception e) {
            log.error("CSV import failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(new CsvImportDto(
                "Import failed: " + e.getMessage(), 0, 1, List.of(e.getMessage()), true
            ));
        }
    }

    private Room mapCsvRecordToRoom(CSVRecord record, User currentUser) {
        String location = record.get("location");
        String managerUsername = record.get("managerUsername");

        // Валидация
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }

        Room room = new Room();
        room.setLocation(location.trim());

        if (managerUsername != null && !managerUsername.trim().isEmpty()) {
            User manager = userService.getUserByUsername(managerUsername.trim());
            if (manager != null) {
                room.setManager(manager);
            } else {
                throw new IllegalArgumentException("Manager not found: " + managerUsername);
            }
        } else {
            //Если менеджер не указан, используем текущего пользователя
            room.setManager(currentUser);
        }

        return room;
    }

    private boolean isCsvFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return "text/csv".equals(contentType) || 
            (fileName != null && fileName.toLowerCase().endsWith(".csv"));
    }
}