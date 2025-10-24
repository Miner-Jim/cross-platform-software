package com.example.demo.controller;

import com.example.demo.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @CookieValue(name = "access_token", required = false)
        String access,
        @CookieValue(name = "refresh_token", required = false)
        String refresh,
        @RequestBody LoginRequest loginRequest) {
            return authService.login(loginRequest, access,refresh);
    }
    
}
