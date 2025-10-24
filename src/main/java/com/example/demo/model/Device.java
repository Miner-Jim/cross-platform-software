package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NotBlank(message = "Название устройства обязательно")
    @Column(nullable = false)
    @ToString.Include
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private DeviceType type;

    @Min(value = 0, message = "Мощность не может быть отрицательной")
    @ToString.Include
    private double power;

    @ToString.Include
    private boolean active;

    @ManyToOne
    @JsonBackReference("room-devices")
    private Room room;

}
