package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Device;
import com.example.demo.model.Room;
import com.example.demo.model.User;
import com.example.demo.repository.RoomRepository;

@Service
public class RoomService {
    private final RoomRepository roomRepository;
    
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> getRoomByLocation(String location) {
        return roomRepository.findByLocationIgnoreCase(location);
    }

    public Room updateRoom(Long id, Room roomDetails) {
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
        if (roomRepository.existsById(id)) {
            roomRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
