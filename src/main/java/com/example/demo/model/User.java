package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    // Связь: Один пользователь может управлять несколькими комнатами
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    @JsonManagedReference("user-rooms")
    private List<Room> managedRooms = new ArrayList<>();

    // Связь с ролями (будет позже)
    // @ManyToMany
    // private Set<Role> roles;
}
