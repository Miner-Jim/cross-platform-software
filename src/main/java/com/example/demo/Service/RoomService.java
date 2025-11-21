package com.example.demo.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.model.Device;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.repository.RoomRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final RoomRepository roomRepository;
    

     public List<Room> getRoomsByManager(Long managerId) {
        logger.debug("Getting rooms by managerId: {}", managerId);
        return roomRepository.findByManagerId(managerId);
    }
    
    public boolean isRoomManager(Long roomId, Long userId) {
        logger.debug("Checking if user {} is manager of room {}", userId, roomId);
        Room room = roomRepository.findById(roomId).orElse(null);
        return room != null && room.getManager() != null && room.getManager().getId().equals(userId);
    }

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(Room room) {
        logger.debug("Creating room: {}", room);
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        logger.info("Getting all rooms");
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        logger.debug("Getting room by id: {}", id);
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> getRoomByLocation(String location) {
        logger.debug("Getting rooms by location: {}", location);
        return roomRepository.findByLocationIgnoreCase(location);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        logger.debug("Updating room with id: {}", id);
        return roomRepository.findById(id)
            .map(existingRoom -> {
                existingRoom.setDevices(roomDetails.getDevices());
                existingRoom.setManager(roomDetails.getManager());
                existingRoom.setLocation(roomDetails.getLocation());
                return roomRepository.save(existingRoom);
            })
            .orElse(null);
        }
    
    public boolean deleteRoom(Long id) {
        logger.debug("Deleting room with id: {}", id);
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            logger.debug("Room with id {} deleted", id);
            return true;
        }
        logger.warn("Room with id {} not found", id);
        return false;
    }


    
}
