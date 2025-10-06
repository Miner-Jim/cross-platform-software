package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import com.example.demo.repository.DeviceRepository;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }
    
    // Сохранить новое устройство
    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }
    
    // Получить все устройства
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }
    
    // Получить устройство по ID
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id).orElse(null);
    }
    
    // Получить устройства по типу
    public List<Device> getDevicesByType(DeviceType type) {
        return deviceRepository.findByType(type);
    }
    
    // Обновить устройство
    public Device updateDevice(Long id, Device deviceDetails) {
        return deviceRepository.findById(id)
            .map(existingDevice -> {
                existingDevice.setTitle(deviceDetails.getTitle());
                existingDevice.setType(deviceDetails.getType());
                existingDevice.setPower(deviceDetails.getPower());
                existingDevice.setActive(deviceDetails.isActive());
                return deviceRepository.save(existingDevice);
            })
            .orElse(null);
    }
    
    // Удалить устройство
    public boolean deleteDevice(Long id) {
        if (deviceRepository.existsById(id)) {
            deviceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
