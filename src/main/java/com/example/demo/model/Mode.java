package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Mode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true) // Название режима должно быть уникальным
    private ModeType modeType; // Тип режима из enum

    // Связь "Один Режим активирует много Устройств"
    // Это ManyToMany, потому что один режим может управлять разными устройствами,
    // и одно устройство может использоваться в разных режимах.
    @ManyToMany
    @JoinTable(
        name = "mode_device",
        joinColumns = @JoinColumn(name = "mode_id"),
        inverseJoinColumns = @JoinColumn(name = "device_id")
    )
    private List<Device> devices = new ArrayList<>();
}
