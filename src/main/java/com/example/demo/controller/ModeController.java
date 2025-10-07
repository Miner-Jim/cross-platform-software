package com.example.demo.controller;

import com.example.demo.service.ModeService;
import com.example.demo.model.ModeType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modes")
public class ModeController {
    private final ModeService modeService;

    public ModeController(ModeService modeService) {
        this.modeService = modeService;
    }

    @PostMapping("/{modeType}/activate")
    public ResponseEntity<String> activateMode(@PathVariable ModeType modeType) {
        String result = modeService.activateMode(modeType);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/night")
    public ResponseEntity<String> activateNightMode() {
        String result = modeService.activateNightMode();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/all-off")
    public ResponseEntity<String> turnOffAll() {
        String result = modeService.turnOffAllDevices();
        return ResponseEntity.ok(result);
    }


    @PostMapping("/all-on")
    public ResponseEntity<String> turnOnAll() {
        String result = modeService.turnOnAllDevices();
        return ResponseEntity.ok(result);
    }
}