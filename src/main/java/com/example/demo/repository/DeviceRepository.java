package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    
    @Query("SELECT d FROM Device d WHERE d.room.manager.id = :managerId")
    Page<Device> findByRoomManagerId(@Param("managerId") Long managerId, Pageable pageable);
    
    
    @Query("SELECT d FROM Device d WHERE d.room.manager.id = :managerId " +
           "AND (:type IS NULL OR d.type = :type) " +
           "AND (:active IS NULL OR d.active = :active)")
    Page<Device> findByManagerIdWithFilter(@Param("managerId") Long managerId,
                                         @Param("type") DeviceType type,
                                         @Param("active") Boolean active,
                                         Pageable pageable);
}
