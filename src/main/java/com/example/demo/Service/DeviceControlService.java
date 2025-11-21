package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
public class DeviceControlService {
    private static final Logger logger = LoggerFactory.getLogger(DeviceControlService.class);
    private final DeviceService deviceService;

    public DeviceControlService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // Включить/выключить устройство по ID
    public Device toggleDevice(Long deviceId, boolean active) {
        logger.info("Attempt to toggle the device");
        Device device = deviceService.getDeviceById(deviceId);
        if (device != null) {
            device.setActive(active);
            logger.debug("The device was toggle: id - {}, status - {}", deviceId, active);
            return deviceService.updateDevice(deviceId, device);
        }
        logger.warn("A non-existent device: id - {}", deviceId);
        return null;
    }

    // Включить/выключить все устройства определенного типа
    public List<Device> toggleDevicesByType(DeviceType type, boolean active) {
        logger.debug("Toggle has been implemented for devices: type - {}, status - {}", type, active);
        List<Device> devices = deviceService.getDevicesByType(type);
        devices.forEach(device -> device.setActive(active));
        // Сохраняем изменения для каждого устройства
        logger.info("Status update for all devices of the same type");
        return devices.stream()
            .map(device -> deviceService.updateDevice(device.getId(), device))
            .toList();
    }

    // Получить суммарное энергопотребление всех включенных устройств
    public double getTotalPowerConsumption() {
        logger.info("Getting the total device consumption");
        List<Device> allDevices = deviceService.getAllDevices();
        return allDevices.stream()
            .filter(Device::isActive)
            .mapToDouble(Device::getPower)
            .sum();
    }

    // Получить устройства по комнате (через DeviceService)
    public List<Device> getDevicesByRoom(Long roomId) {
        logger.info("Getting devices by room");
        return deviceService.getAllDevices().stream()
            .filter(device -> device.getRoom() != null && device.getRoom().getId().equals(roomId))
            .toList();
    }

    // Получить все устройства (делегируем DeviceService)
    public List<Device> getAllDevices() {
        logger.info("Getting all the devices");
        return deviceService.getAllDevices();
    }
}