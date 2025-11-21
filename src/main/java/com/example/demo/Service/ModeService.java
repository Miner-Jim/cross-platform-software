package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.ModeRepository;
import com.example.demo.repository.ModeRuleRepository;

import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ModeService {
    private static final Logger logger = LoggerFactory.getLogger(ModeService.class);
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
            logger.warn("There are no rules configured for this mode");
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
        logger.debug("{} mode activated, devices changed: {}",  modeType, devicesChanged);
        return String.format("Режим '%s' активирован. Изменено устройств: %d", modeType, devicesChanged);
    }

    // отключаем все устройства кроме климат-контроля
    public String activateNightMode() {
        log.info("Attempt to activate night mode");
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOff = 0;
        
        for (Device device : allDevices) {
            boolean isClimateDevice = device.getType() == DeviceType.CONDITIONER;
            
            if (!isClimateDevice && device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), false);
                devicesTurnedOff++;
            }
        }
        logger.debug("Night mode activated, devices turned off: {}", devicesTurnedOff);
        return String.format("Ночной режим активирован. Выключено устройств: %d", devicesTurnedOff);
    }

    // отключаем все устройства
    public String turnOffAllDevices() {
        log.info("Attempt to turn off all devices");
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOff = 0;
        
        for (Device device : allDevices) {
            if (device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), false);
                devicesTurnedOff++;
            }
        }
        log.warn("All devices are disabled: {}", devicesTurnedOff);
        return String.format("Все устройства выключены. Отключено: %d", devicesTurnedOff);
    }

    // включение всех устройств
    public String turnOnAllDevices() {
        log.info("Attempt to turn on all devices");
        List<Device> allDevices = deviceControlService.getAllDevices();
        int devicesTurnedOn = 0;
        
        for (Device device : allDevices) {
            if (!device.isActive()) {
                deviceControlService.toggleDevice(device.getId(), true);
                devicesTurnedOn++;
            }
        }
        log.warn("All devices are enabled: {}", devicesTurnedOn);
        return String.format("Все устройства включены. Включено: %d", devicesTurnedOn);
    }


    private Boolean evaluateDeviceState(Device device, List<ModeRule> rules) {
        logger.info("Device status assessment");
        for (ModeRule rule : rules) {
            if (matchesRule(device, rule)) {
                return rule.getShouldBeActive();
            }
        }
        logger.warn("None of the rules fit");
        return null;
    }

    private boolean matchesRule(Device device, ModeRule rule) {
        logger.info("Rule assessment");
        if (rule.getDeviceType() != null && device.getType() != rule.getDeviceType()) {
            logger.warn("Device type does not match");
            return false;
        }

        if (rule.getTitlePattern() != null && !rule.getTitlePattern().isEmpty()) {
            Pattern pattern = Pattern.compile(rule.getTitlePattern(), Pattern.CASE_INSENSITIVE);
            if (!pattern.matcher(device.getTitle()).find()) {
                logger.warn("Device title pattern does not match");
                return false;
            }
        }

        if (rule.getMinPower() != null && device.getPower() < rule.getMinPower()) {
            logger.warn("Device power is too low");
            return false;
        }
        if (rule.getMaxPower() != null && device.getPower() > rule.getMaxPower()) {
            logger.warn("Device power is too high");
            return false;
        }
        logger.info("Rules matches");
        return true;
    }
}