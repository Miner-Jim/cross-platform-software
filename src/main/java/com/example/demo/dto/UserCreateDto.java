package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
    @NotBlank @Size(min = 2, max = 20) String username,
    @NotBlank String password,
    Long roleId  // ID роли для пользователя
) {}
