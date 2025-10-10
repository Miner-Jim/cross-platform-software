package com.example.demo.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device>{
    // Найти все устройства по типу
    List<Device> findByType(DeviceType type);
    
    // Найти все устройства по названию (игнорируя регистр)
    List<Device> findByTitleContainingIgnoreCase(String title);
    
    // Найти все включенные устройства
    List<Device> findByActiveTrue();
    
    // Найти устройства в определенной комнате (по id комнаты)
    List<Device> findByRoomId(Long roomId);
}
