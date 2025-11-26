package com.example.demo.dto;

import com.example.demo.model.DeviceType;

public record DeviceRequestDto(
    String title,
    DeviceType type,
    double power,
    boolean active,
    Long roomId
) {}