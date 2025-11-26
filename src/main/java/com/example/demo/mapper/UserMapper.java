package com.example.demo.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserLoggedDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.model.Permission;
import com.example.demo.model.User;

public class UserMapper {
    public static UserDto userToUserDto(User user) {
        return new UserDto(user.getId(),
        user.getUsername(),
        user.getPassword(),
        user.getRole().getAuthority(),
        user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }

    public static UserLoggedDto userToUserLoggedDTO(User user) {
        return new UserLoggedDto(
        user.getUsername(),
        user.getRole().getAuthority(),
        user.getRole().getPermissions().stream().map(Permission::getAuthority).collect(Collectors.toSet()));
    }

     public static UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getRole() != null ? user.getRole().getName() : null,
            user.getRole() != null ? 
                user.getRole().getPermissions().stream()
                    .map(Permission::getAuthority)
                    .collect(Collectors.toSet()) : 
                Set.of()
        );
    }
}
