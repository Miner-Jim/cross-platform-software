package com.example.demo.dto;

public record UserSimpleDto(
    Long id,
    String username,
    String role
) {}