package com.example.demo;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.model.Permission;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.PermissionRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication implements ApplicationRunner {
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // userRepository.deleteAll();
        // roleRepository.deleteAll();
        // permissionRepository.deleteAll();

        // Создаем права для УМНОГО ДОМА
        Permission userRead = createPermissionIfNotFound("USER", "READ");
        Permission userWrite = createPermissionIfNotFound("USER", "WRITE");
        Permission deviceRead = createPermissionIfNotFound("DEVICE", "READ");
        Permission deviceWrite = createPermissionIfNotFound("DEVICE", "WRITE");
        Permission roomRead = createPermissionIfNotFound("ROOM", "READ");
        Permission roomWrite = createPermissionIfNotFound("ROOM", "WRITE");
        Permission modeControl = createPermissionIfNotFound("MODE", "CONTROL");

        // роли с правильными permissions для УМНОГО ДОМА
        Role userRole = createRoleIfNotFound("USER", Set.of(
            deviceRead, roomRead
        ));
        
        Role adminRole = createRoleIfNotFound("ADMIN", Set.of(
            userRead, userWrite, deviceRead, deviceWrite, 
            roomRead, roomWrite, modeControl
        ));

        // Создаем пользователей с правильными ролями
        createUserIfNotFound("user", "user", userRole);
        createUserIfNotFound("admin", "admin", adminRole);

        System.out.println("Умный дом: Данные инициализированы!");
        System.out.println("USER: user/user -> USER (только просмотр)");
        System.out.println("ADMIN: admin/admin -> ADMIN (полные права)");
    }

    private Permission createPermissionIfNotFound(String resource, String operation) {
        Permission permission = permissionRepository.findByResourceAndOperation(resource, operation);
        if (permission == null) {
            permission = new Permission(resource, operation);
            permission = permissionRepository.save(permission);
            System.out.println("Создано право: " + resource + ":" + operation);
        }
        return permission;
    }

    private Role createRoleIfNotFound(String name, Set<Permission> permissions) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            role = roleRepository.save(role);
            System.out.println("Создана роль: " + name + " с " + permissions.size() + " правами");
        }
        return role;
    }

    private void createUserIfNotFound(String username, String password, Role role) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .build();
            userRepository.save(user);
            System.out.println("Создан пользователь: " + username + " с ролью: " + role.getName());
        } else {
            System.out.println("Пользователь уже существует: " + username);
        }
    }
}