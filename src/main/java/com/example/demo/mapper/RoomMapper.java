package com.example.demo.mapper;

import com.example.demo.dto.RoomDto;
import com.example.demo.dto.UserSimpleDto;

import java.util.List;

import com.example.demo.dto.DeviceSimpleDto;
import com.example.demo.model.Room;
import com.example.demo.model.Device;

public class RoomMapper {
    
    public static RoomDto toDto(Room room) {
        if (room == null) {
            return null;
        }
        
        return new RoomDto(
            room.getId(),
            room.getLocation(),
            room.getManager() != null ? 
                new UserSimpleDto(
                    room.getManager().getId(),
                    room.getManager().getUsername(),
                    room.getManager().getRole() != null ? room.getManager().getRole().getName() : null
                ) : null,
            room.getDevices() != null ? 
                room.getDevices().stream()
                    .map(RoomMapper::toDeviceSimpleDto)
                    .toList() : 
                List.of()
        );
    }
    
    private static DeviceSimpleDto toDeviceSimpleDto(Device device) {
        return new DeviceSimpleDto(
            device.getId(),
            device.getTitle(),
            device.getType(),
            device.getPower(),
            device.isActive()
        );
    }
}