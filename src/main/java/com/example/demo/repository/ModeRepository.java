package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Mode;
import com.example.demo.model.ModeType;

@Repository
public interface ModeRepository extends JpaRepository<Mode, Long> {
    Optional<Mode> findByModeType(ModeType modeType);
}
