package com.example.demo.controller;

import com.example.demo.service.ModeService;

import lombok.extern.slf4j.Slf4j;

import com.example.demo.model.ModeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/modes")
public class ModeController {
    private final ModeService modeService;
    private static final Logger logger = LoggerFactory.getLogger(ModeController.class);

    public ModeController(ModeService modeService) {
        this.modeService = modeService;
    }

    @PostMapping("/{modeType}/activate")
    public ResponseEntity<String> activateMode(@PathVariable ModeType modeType) {
        logger.debug("POST/{modeType}/activate");
        String result = modeService.activateMode(modeType);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/night")
    public ResponseEntity<String> activateNightMode() {
        logger.debug("POST/night");
        String result = modeService.activateNightMode();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/all-off")
    public ResponseEntity<String> turnOffAll() {
        logger.debug("POST/all-off");
        String result = modeService.turnOffAllDevices();
        return ResponseEntity.ok(result);
    }


    @PostMapping("/all-on")
    public ResponseEntity<String> turnOnAll() {
        logger.debug("POST/all-on");
        String result = modeService.turnOnAllDevices();
        return ResponseEntity.ok(result);
    }
}