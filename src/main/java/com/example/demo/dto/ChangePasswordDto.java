package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
    @NotBlank(message = "Текущий пароль обязателен")
    String currentPassword,
    
    @NotBlank(message = "Новый пароль обязателен")
    @Size(min = 6, message = "Новый пароль должен быть не менее 6 символов")
    String newPassword,
    
    @NotBlank(message = "Подтверждение пароля обязательно")
    String confirmPassword
) {}