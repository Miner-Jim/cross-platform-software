package com.example.demo.repository;

import com.example.demo.model.ModeRule;
import com.example.demo.model.ModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModeRuleRepository extends JpaRepository<ModeRule, Long> {
    List<ModeRule> findByModeTypeOrderByPriorityDesc(ModeType modeType);
}