package com.example.demo.controller;

import com.example.demo.model.ModeRule;
import com.example.demo.model.ModeType;
import com.example.demo.repository.ModeRuleRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mode-rules")
@Tag(name = "Сценарии (правила)")
public class ModeRuleController {
    private static final Logger logger = LoggerFactory.getLogger(ModeRuleController.class);
    private final ModeRuleRepository modeRuleRepository;

    public ModeRuleController(ModeRuleRepository modeRuleRepository) {
        this.modeRuleRepository = modeRuleRepository;
    }
    @Operation(summary = "Создать правило")
    @PostMapping
    public ResponseEntity<ModeRule> createRule(@RequestBody ModeRule rule) {
        logger.debug("POST/api/mode-rules");
        ModeRule savedRule = modeRuleRepository.save(rule);
        return ResponseEntity.ok(savedRule);
    }
    @Operation(summary = "Получить правило по типу")
    @GetMapping("/mode/{modeType}")
    public ResponseEntity<List<ModeRule>> getRulesByMode(@PathVariable ModeType modeType) {
        logger.debug("GET/api/mode-rules/mode/{}", modeType);
        List<ModeRule> rules = modeRuleRepository.findByModeTypeOrderByPriorityDesc(modeType);
        return ResponseEntity.ok(rules);
    }
    @Operation(summary = "Получить все правила")
    @GetMapping
    public ResponseEntity<List<ModeRule>> getAllRules() {
        logger.debug("GET/api/mode-rules");
        List<ModeRule> rules = modeRuleRepository.findAll();
        return ResponseEntity.ok(rules);
    }
    @Operation(summary = "Обновить правило по ID")
    @PutMapping("/{id}")
    public ResponseEntity<ModeRule> updateRule(@PathVariable Long id, @RequestBody ModeRule ruleDetails) {
        logger.debug("PUT/api/mode-rules/{}", id);
        return modeRuleRepository.findById(id)
            .map(existingRule -> {
                existingRule.setModeType(ruleDetails.getModeType());
                existingRule.setDeviceType(ruleDetails.getDeviceType());
                existingRule.setTitlePattern(ruleDetails.getTitlePattern());
                existingRule.setMinPower(ruleDetails.getMinPower());
                existingRule.setMaxPower(ruleDetails.getMaxPower());
                existingRule.setShouldBeActive(ruleDetails.getShouldBeActive());
                existingRule.setPriority(ruleDetails.getPriority());
                return ResponseEntity.ok(modeRuleRepository.save(existingRule));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    @Operation(summary = "Удалить правило по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        logger.debug("DELETE/api/mode-rules/{}", id);
        if (modeRuleRepository.existsById(id)) {
            modeRuleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            logger.warn("Rule with id {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }
}