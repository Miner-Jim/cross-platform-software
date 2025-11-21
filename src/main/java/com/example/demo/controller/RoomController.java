package com.example.demo.controller;

import com.example.demo.dto.RoomDto;
import com.example.demo.dto.RoomCreateDto;
import com.example.demo.mapper.RoomMapper;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.service.RoomService;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private static final Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService roomService;
    private final UserService userService;

    public RoomController(RoomService roomService, UserService userService)  {
        this.roomService = roomService;
        this.userService = userService;
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
}