package com.example.barbershop.dto;

import lombok.Data;

/**
 * DTO для запроса аутентификации (логина).
 * Содержит данные, которые клиент отправляет при входе в систему.
 */
@Data
public class LoginRequest {

    /**
     * Email пользователя (используется как логин).
     * Валидация: должен быть не пустым и соответствовать формату email.
     */
    private String email;

    /**
     * Пароль пользователя.
     * Валидация: должен быть не пустым.
     */
    private String password;
}