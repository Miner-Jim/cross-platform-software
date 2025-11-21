package com.example.demo.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.specification.DeviceSpecification;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    public Page<Device> getDevicesByFilter(String title, DeviceType type, 
                                         Double minPower, Double maxPower, 
                                         Boolean active, Pageable pageable) {
        logger.info("Devices are filtered by fields");
        Specification<Device> spec = DeviceSpecification.withFilter(title, type, minPower, maxPower, active);
        return deviceRepository.findAll(spec, pageable);
    }

    // Сохранить новое устройство
    public Device createDevice(Device device) {
        logger.debug("Creating a device: {}", device.getTitle());
        Device savedDevice = deviceRepository.save(device);
        logger.debug("The device has been created: ID={}", savedDevice.getId());
        return savedDevice;
    }
    
    // Получить все устройства
    public List<Device> getAllDevices() {
        logger.info("All devices search");
        return deviceRepository.findAll();
    }
    
    // Получить устройство по ID
    public Device getDeviceById(Long id) {
        logger.debug("Device ID search: {}", id);
        return deviceRepository.findById(id).orElse(null);
    }
    
    // Получить устройства по типу
    public List<Device> getDevicesByType(DeviceType type) {
        logger.debug("Device by type search: {}", type);
        return deviceRepository.findByType(type);
    }
    
    // Обновить устройство
    public Device updateDevice(Long id, Device deviceDetails) {
        logger.debug("Updating the device ID: {}", id);
        return deviceRepository.findById(id)
            .map(existingDevice -> {
                existingDevice.setTitle(deviceDetails.getTitle());
                existingDevice.setType(deviceDetails.getType());
                existingDevice.setPower(deviceDetails.getPower());
                existingDevice.setActive(deviceDetails.isActive());
                logger.debug("Device ID {} updated", id);
                return deviceRepository.save(existingDevice);
            })
            .orElse(null);
    }
    
    // Удалить устройство
    public boolean deleteDevice(Long id) {
        logger.debug("Deleting the device ID: {}", id);
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            logger.debug("Device ID {} deleted", id);
            return true;
        }
        logger.warn("Device ID {} not found for deletion", id);
        return false;
    }


   
    public Page<Device> getDevicesByUserRooms(Long managerId, Pageable pageable) {
        logger.debug("Device search by user: {}", managerId);
        return deviceRepository.findByRoomManagerId(managerId, pageable);
    }
    
    
    public Page<Device> getDevicesByUserRoomsWithFilter(Long managerId, String title, DeviceType type, 
                                                      Double minPower, Double maxPower, Boolean active, 
                                                      Pageable pageable) {
        //Пока используем фильтрацию только по type и active
        logger.info("Devices by user are filtered by fields");
        return deviceRepository.findByManagerIdWithFilter(managerId, type, active, pageable);
    }
}

