package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@ToString(onlyExplicitlyIncluded = true)
public class User implements UserDetails{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include  // ✅ ЯВНО ВКЛЮЧАЕМ
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank()
    @Size(min = 2, max = 20)
    @ToString.Include  // ✅ ЯВНО ВКЛЮЧАЕМ
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    @JsonManagedReference("user-rooms")
    // ✅ НЕТ @ToString.Include - НЕ ВКЛЮЧАЕТСЯ В toString()
    private List<Room> managedRooms = new ArrayList<>();

    @ManyToOne
    // ✅ НЕТ @ToString.Include - НЕ ВКЛЮЧАЕТСЯ В toString()
    private Role role;

    @OneToMany
    // ✅ НЕТ @ToString.Include - НЕ ВКЛЮЧАЕТСЯ В toString()
    private Set<Token> tokens;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorities = new HashSet<>();
        role.getPermissions().forEach(permission -> {
            authorities.add(permission.getAuthority());
        });
        authorities.add(role.getAuthority());
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

}
