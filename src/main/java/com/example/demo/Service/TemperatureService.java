package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.DeviceType;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TemperatureService {
    private final DeviceControlService deviceControlService;

    public TemperatureService(DeviceControlService deviceControlService) {
        this.deviceControlService = deviceControlService;
    }

    public String controlByTemperature(Long roomId, double currentTemperature) {
        List<Device> roomDevices = deviceControlService.getDevicesByRoom(roomId);
        Device conditioner = findConditioner(roomDevices);
        
        if (conditioner == null) {
            return "Кондиционер не найден в комнате";
        }

        // управление при определенной температуре
        if (currentTemperature > 25.0) {

            if (!conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), true);
                return String.format("Кондиционер включен. Температура: %.1f°C", currentTemperature);
            }
            return "Кондиционер уже работает";
            
        } else if (currentTemperature < 20.0) {

            if (conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), false);
                return String.format("Кондиционер выключен. Температура: %.1f°C", currentTemperature);
            }
            return "Кондиционер уже выключен";
            
        } else {

            if (conditioner.isActive()) {
                deviceControlService.toggleDevice(conditioner.getId(), false);
                return String.format("Комфортная температура достигнута: %.1f°C. Кондиционер выключен", currentTemperature);
            }
            return String.format("Комфортная температура: %.1f°C", currentTemperature);
        }
    }

    private Device findConditioner(List<Device> devices) {
        return devices.stream()
            .filter(device -> device.getType() == DeviceType.CONDITIONER)
            .findFirst()
            .orElse(null);
    }
}