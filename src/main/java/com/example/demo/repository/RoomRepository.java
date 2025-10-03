package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>{
    List<Room> findByLocationIgnoreCase(String location);
}
