package com.example.demo.controller;

import com.example.demo.dto.ChangePasswordDto;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthenticationController {

    private final AuthService authService;
    @Operation(summary = "Вход в систему")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @CookieValue(name = "access_token", required = false) String access,
        @CookieValue(name = "refresh_token", required = false) String refresh,
        @RequestBody LoginRequest loginRequest) {
            return authService.login(loginRequest, access, refresh);
    }
    @Operation(summary = "Обновление токена")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
        @CookieValue(name = "refresh_token", required = false) String refresh) {
            return authService.refresh(refresh);
    }
    @Operation(summary = "Выход из системы")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(
        @CookieValue(name = "access_token", required = false) String access) {
        return authService.logout(access);
    }
    @Operation(summary = "Смена пароля")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordDto changePasswordDto,
            Authentication authentication) {
        
        return authService.changePassword(changePasswordDto, authentication.getName());
    }
}