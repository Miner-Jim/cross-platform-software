package com.example.demo.controller;

import com.example.demo.service.ModeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.example.demo.model.ModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/modes")
@Tag(name = "Режимы")
public class ModeController {
    private final ModeService modeService;
    private static final Logger logger = LoggerFactory.getLogger(ModeController.class);

    public ModeController(ModeService modeService) {
        this.modeService = modeService;
    }
    @Operation(summary = "Активировать режим")
    @PostMapping("/{modeType}/activate")
    public ResponseEntity<String> activateMode(@PathVariable ModeType modeType) {
        logger.debug("POST/{modeType}/activate");
        String result = modeService.activateMode(modeType);
        return ResponseEntity.ok(result);
    }
    @Operation(summary = "Активировать ночной режим")
    @PostMapping("/night")
    public ResponseEntity<String> activateNightMode() {
        logger.debug("POST/night");
        String result = modeService.activateNightMode();
        return ResponseEntity.ok(result);
    }
    @Operation(summary = "Выключить все")
    @PostMapping("/all-off")
    public ResponseEntity<String> turnOffAll() {
        logger.debug("POST/all-off");
        String result = modeService.turnOffAllDevices();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Включить все")
    @PostMapping("/all-on")
    public ResponseEntity<String> turnOnAll() {
        logger.debug("POST/all-on");
        String result = modeService.turnOnAllDevices();
        return ResponseEntity.ok(result);
    }
}