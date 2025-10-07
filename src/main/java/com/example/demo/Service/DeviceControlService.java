package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DeviceControlService {
    private final DeviceService deviceService;

    public DeviceControlService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Включить/выключить устройство по ID
    public Device toggleDevice(Long deviceId, boolean active) {
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            device.setActive(active);
            return deviceService.updateDevice(deviceId, device);
        }
        return null;
    }

    // Включить/выключить все устройства определенного типа
    public List<Device> toggleDevicesByType(DeviceType type, boolean active) {
        List<Device> devices = deviceService.getDevicesByType(type);
        devices.forEach(device -> device.setActive(active));
        // Сохраняем изменения для каждого устройства
        return devices.stream()
            .map(device -> deviceService.updateDevice(device.getId(), device))
            .toList();
    }

    // Получить суммарное энергопотребление всех включенных устройств
    public double getTotalPowerConsumption() {
        List<Device> allDevices = deviceService.getAllDevices();
        return allDevices.stream()
            .filter(Device::isActive)
            .mapToDouble(Device::getPower)
            .sum();
    }

    // Получить устройства по комнате (через DeviceService)
    public List<Device> getDevicesByRoom(Long roomId) {
        return deviceService.getAllDevices().stream()
            .filter(device -> device.getRoom() != null && device.getRoom().getId().equals(roomId))
            .toList();
    }

    // Получить все устройства (делегируем DeviceService)
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }
}