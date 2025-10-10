package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank()
    @Size(min = 2, max = 20)
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 20)
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
