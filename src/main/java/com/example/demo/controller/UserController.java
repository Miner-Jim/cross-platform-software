package com.example.demo.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users - получить всех пользователей
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        logger.info("Getting all users");
        try {
            List<User> users = userService.getAllUsers();
            List<UserResponseDto> userDtos = users.stream()
                    .map(UserMapper::toUserResponseDto)
                    .toList();
            return ResponseEntity.ok(userDtos);
        } catch (Exception e) {
            logger.error("Error getting all users: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/users/{id} - получить пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        logger.debug("GET /api/users/{}", id);
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                return ResponseEntity.ok(UserMapper.toUserResponseDto(user));
            } else {
                logger.warn("User with id {} not found", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting user by id {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/users - создать пользователя (основной метод)
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserCreateDto userCreateDto) {
        logger.debug("POST /api/users - {}", userCreateDto);
        try {
            User createdUser = userService.createUserWithRole(userCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserMapper.toUserResponseDto(createdUser));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error creating user: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // POST /api/users/create - альтернативный метод создания
    @PostMapping("/create")
    public ResponseEntity<UserResponseDto> createUserAlternative(@RequestBody UserCreateDto userCreateDto) {
        logger.debug("POST /api/users/create - {}", userCreateDto);
        try {
            User createdUser = userService.createUserWithRole(userCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserMapper.toUserResponseDto(createdUser));
        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error creating user: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // PUT /api/users/{id} - обновить пользователя
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, 
                                                     @RequestBody UserCreateDto userCreateDto) {
        logger.debug("PUT /api/users/{} - {}", id, userCreateDto);
        try {
            // Создаем временный User объект из DTO для обновления
            User userDetails = new User();
            userDetails.setUsername(userCreateDto.username());
            userDetails.setPassword(userCreateDto.password()); // Пароль будет закодирован в сервисе
            
            
            User updatedUser = userService.updateUser(id, userDetails);
            if (updatedUser != null) {
                return ResponseEntity.ok(UserMapper.toUserResponseDto(updatedUser));
            } else {
                logger.warn("User with id {} not found for update", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating user {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // DELETE /api/users/{id} - удалить пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.debug("DELETE /api/users/{}", id);
        try {
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                logger.info("User with id {} successfully deleted", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("User with id {} not found for deletion", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // GET /api/users/username/{username} - дополнительный метод для поиска по username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        logger.debug("GET /api/users/username/{}", username);
        try {
            User user = userService.getUserByUsername(username);
            if (user != null) {
                return ResponseEntity.ok(UserMapper.toUserResponseDto(user));
            } else {
                logger.warn("User with username {} not found", username);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error getting user by username {}: {}", username, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}