package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.ModeRepository;
import com.example.demo.repository.ModeRuleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class ModeService {
    private final ModeRepository modeRepository;
    private final ModeRuleRepository modeRuleRepository;
    private final DeviceControlService deviceControlService;

    public ModeService(ModeRepository modeRepository, 
                      ModeRuleRepository modeRuleRepository,
                      DeviceControlService deviceControlService) {
        this.modeRepository = modeRepository;
        this.modeRuleRepository = modeRuleRepository;
        this.deviceControlService = deviceControlService;
    }

    // активация режима по заданному правилу
    public String activateMode(ModeType modeType) {
        List<ModeRule> rules = modeRuleRepository.findByModeTypeOrderByPriorityDesc(modeType);
        
        if (rules.isEmpty()) {
            return "Для данного режима не настроены правила";
        }

        List<Device> allDevices = deviceControlService.getAllDevices();
        
        int devicesChanged = 0;
        for (Device device : allDevices) {
            Boolean shouldBeActive = evaluateDeviceState(device, rules);
            if (shouldBeActive != null && device.isActive() != shouldBeActive) {
                deviceControlService.toggleDevice(device.getId(), shouldBeActive);
                devicesChanged++;
            }
        }

        return String.format("Режим '%s' активирован. Изменено устройств: %d", modeType, devicesChanged);
    }

    // отключаем все устройства кроме климат-контроля
    public String activateNightMode() {
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOff = 0;
        
        for (Device device : allDevices) {
            boolean isClimateDevice = device.getType() == DeviceType.CONDITIONER;
            
            if (!isClimateDevice && device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), false);
                devicesTurnedOff++;
            }
        }
        
        return String.format("Ночной режим активирован. Выключено устройств: %d", devicesTurnedOff);
    }

    // отключаем все устройства
    public String turnOffAllDevices() {
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOff = 0;
        
        for (Device device : allDevices) {
            if (device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), false);
                devicesTurnedOff++;
            }
        }
        
        return String.format("Все устройства выключены. Отключено: %d", devicesTurnedOff);
    }

    // включение всех устройств
    public String turnOnAllDevices() {
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOn = 0;
        
        for (Device device : allDevices) {
            if (!device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), true);
                devicesTurnedOn++;
            }
        }
        
        return String.format("Все устройства включены. Включено: %d", devicesTurnedOn);
    }


    private Boolean evaluateDeviceState(Device device, List<ModeRule> rules) {
        for (ModeRule rule : rules) {
            if (matchesRule(device, rule)) {
                return rule.getShouldBeActive();
            }
        }
        return null;
    }

    private boolean matchesRule(Device device, ModeRule rule) {
        if (rule.getDeviceType() != null && device.getType() != rule.getDeviceType()) {
            return false;
        }

        if (rule.getTitlePattern() != null && !rule.getTitlePattern().isEmpty()) {
            Pattern pattern = Pattern.compile(rule.getTitlePattern(), Pattern.CASE_INSENSITIVE);
            if (!pattern.matcher(device.getTitle()).find()) {
                return false;
            }
        }

        if (rule.getMinPower() != null && device.getPower() < rule.getMinPower()) {
            return false;
        }
        if (rule.getMaxPower() != null && device.getPower() > rule.getMaxPower()) {
            return false;
        }

        return true;
    }
}