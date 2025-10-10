package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manager_id") // Внешний ключ в таблице room
    @JsonBackReference("user-rooms")
    private User manager;

    @Column(nullable = false)
    @NotBlank(message = "Название комнаты не может быть пустым")
    private String location;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonManagedReference("room-devices")
    private List<Device> devices = new ArrayList<>();


}
