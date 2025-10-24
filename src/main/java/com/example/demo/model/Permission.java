package com.example.demo.model;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements GrantedAuthority{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resource;

    private String operation;

    @ManyToMany
    private Set<Role> roles;

    @Override
    public String getAuthority() {
        return String.format("%S:%S", resource.toUpperCase(), operation.toUpperCase());
    }

    public Permission(String string, String string2) {
        this.resource = string;
        this.operation = string2;
    }

}
