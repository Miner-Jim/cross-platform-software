package com.example.demo.dto;

import com.example.demo.model.DeviceType;
import java.util.List;

public record RoomDto(
    Long id,
    String location,
    UserSimpleDto manager,
    List<DeviceSimpleDto> devices
) {}