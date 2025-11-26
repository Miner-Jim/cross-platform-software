package com.example.demo.dto;

import java.util.Set;

public record UserResponseDto(
    Long id,
    String username,
    String role,
    Set<String> permissions
) {}