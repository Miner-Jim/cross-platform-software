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
import com.example.demo.model.User;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@RequestMapping ("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Getting all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.debug("GET/api/users/id{}", id);
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            logger.warn("User with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.debug("POST/api/users - {}", user);
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserCreateDto userCreateDto) {
        logger.debug("POST/api/users/create - {}", userCreateDto);
        try {
            User createdUser = userService.createUserWithRole(userCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } 
        catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        logger.debug("PUT/api/users/{} - {}", id, userDetails);
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            logger.warn("User with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        logger.debug("DELETE/api/users/{}", id);
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("User with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

}
