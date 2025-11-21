package com.example.demo.dto;

import com.example.demo.model.DeviceType;

public record DeviceSimpleDto(
    Long id,
    String title,
    DeviceType type,
    double power,
    boolean active
) {}