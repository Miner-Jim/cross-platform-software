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
public class TemperatureService {
    private static final Logger logger = LoggerFactory.getLogger(TemperatureService.class);
    private final DeviceControlService deviceControlService;

    public TemperatureService(DeviceControlService deviceControlService) {
        this.deviceControlService = deviceControlService;
    }

    public String controlByTemperature(Long roomId, double currentTemperature) {
        logger.debug("Control by temperature for room {}", roomId);
        List<Device> roomDevices = deviceControlService.getDevicesByRoom(roomId);
        Device conditioner = findConditioner(roomDevices);
        
        if (conditioner == null) {
            logger.warn("Conditioner not found in room {}", roomId);
            return "Кондиционер не найден в комнате";
        }

        // управление при определенной температуре
        if (currentTemperature > 25.0) {

            if (!conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), true);
                logger.debug("Conditioner turned on for room {}", roomId);
                return String.format("Кондиционер включен. Температура: %.1f°C", currentTemperature);
            }
            logger.debug("Conditioner is already on for room {}", roomId);
            return "Кондиционер уже работает";
            
        } else if (currentTemperature < 20.0) {

            if (conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), false);
                logger.debug("Conditioner turned off for room {}", roomId);
                return String.format("Кондиционер выключен. Температура: %.1f°C", currentTemperature);
            }
            logger.debug("Conditioner is already off for room {}", roomId);
            return "Кондиционер уже выключен";
            
        } else {

            if (conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), false);
                logger.debug("Conditioner turned off for room because comfort temperature{}", roomId);
                return String.format("Комфортная температура достигнута: %.1f°C. Кондиционер выключен", currentTemperature);
            }
            logger.debug("A comfortable temperature has been reached: {} °C", currentTemperature);
            return String.format("Комфортная температура: %.1f°C", currentTemperature);
        }
    }

    private Device findConditioner(List<Device> devices) {
        logger.info("Finding conditioner in room");
        return devices.stream()
            .filter(device -> device.getType() == DeviceType.CONDITIONER)
            .findFirst()
            .orElse(null);
    }
}