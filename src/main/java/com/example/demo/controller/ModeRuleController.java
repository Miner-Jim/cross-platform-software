package com.example.demo.controller;

import com.example.demo.model.ModeRule;
import com.example.demo.model.ModeType;
import com.example.demo.repository.ModeRuleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mode-rules")
public class ModeRuleController {
    private final ModeRuleRepository modeRuleRepository;

    public ModeRuleController(ModeRuleRepository modeRuleRepository) {
        this.modeRuleRepository = modeRuleRepository;
    }

    @PostMapping
    public ResponseEntity<ModeRule> createRule(@RequestBody ModeRule rule) {
        ModeRule savedRule = modeRuleRepository.save(rule);
        return ResponseEntity.ok(savedRule);
    }

    @GetMapping("/mode/{modeType}")
    public ResponseEntity<List<ModeRule>> getRulesByMode(@PathVariable ModeType modeType) {
        List<ModeRule> rules = modeRuleRepository.findByModeTypeOrderByPriorityDesc(modeType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping
    public ResponseEntity<List<ModeRule>> getAllRules() {
        List<ModeRule> rules = modeRuleRepository.findAll();
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeRule> updateRule(@PathVariable Long id, @RequestBody ModeRule ruleDetails) {
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (modeRuleRepository.existsById(id)) {
            modeRuleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}