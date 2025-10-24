package com.example.demo.service;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // создание пользователя с валидацией
    public User createUserWithRole(UserCreateDto userCreateDto) {
        if (userRepository.existsByUsername(userCreateDto.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        Role role = roleRepository.findById(userCreateDto.roleId())
            .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        
        User user = User.builder()
            .username(userCreateDto.username())
            .password(passwordEncoder.encode(userCreateDto.password()))
            .role(role)
            .build();
            
        return userRepository.save(user);
    }

    //Создание администратора
    public User createAdminUser(String username, String password) {
        Role adminRole = roleRepository.findByName("ADMIN");
        if (adminRole == null) {
            throw new IllegalStateException("ADMIN role not found");
        }
        
        User user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .role(adminRole)
            .build();
            
        return userRepository.save(user);
    }

    
    public User toggleUserStatus(Long userId, boolean enabled) {
        User user = getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return userRepository.save(user);
    }

    // Остальные методы остаются как есть
    public User createUser(User user) {  
        user.setPassword(passwordEncoder.encode(user.getPassword())); //Кодируем пароль
        return userRepository.save(user);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
            .map(existingUser -> {
                existingUser.setUsername(userDetails.getUsername());
                existingUser.setPassword(userDetails.getPassword());
                // existingUser.setEnabled(userDetails.isEnabled());
                return userRepository.save(existingUser);
            })
            .orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}