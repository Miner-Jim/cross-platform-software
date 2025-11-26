package com.example.demo.dto;

import com.example.demo.model.DeviceType;

public record DeviceResponseDto(
    Long id,
    String title,
    DeviceType type,
    double power,
    boolean active,
    RoomSimpleDto room
) {}