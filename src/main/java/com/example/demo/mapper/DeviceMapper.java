package com.example.demo.mapper;

import com.example.demo.dto.DeviceResponseDto;
import com.example.demo.dto.RoomSimpleDto;
import com.example.demo.model.Device;

public class DeviceMapper {
    
    public static DeviceResponseDto toDto(Device device) {
        if (device == null) {
            return null;
        }
        
        return new DeviceResponseDto(
            device.getId(),
            device.getTitle(),
            device.getType(),
            device.getPower(),
            device.isActive(),
            device.getRoom() != null ? 
                new RoomSimpleDto(device.getRoom().getId(), device.getRoom().getLocation()) : 
                null
        );
    }
}