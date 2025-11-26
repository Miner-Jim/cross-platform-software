package com.example.demo.dto;

public record TemperatureControlDto(
    Long roomId,
    Double temperature
) {}