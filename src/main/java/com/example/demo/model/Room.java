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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    @JsonBackReference("user-rooms")
    // ✅ НЕТ @ToString.Include
    private User manager;

    @Column(nullable = false)
    @NotBlank(message = "Название комнаты не может быть пустым")
    @ToString.Include
    private String location;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    @JsonManagedReference("room-devices")
    // ✅ НЕТ @ToString.Include
    private List<Device> devices = new ArrayList<>();


}
