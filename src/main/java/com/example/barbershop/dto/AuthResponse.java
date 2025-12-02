package com.example.barbershop.dto;

import lombok.Data;

/**
 * DTO для ответа после успешной аутентификации.
 * Содержит данные, которые сервер возвращает клиенту после входа.
 */
@Data
public class AuthResponse {

    /**
     * JWT токен для доступа к защищенным endpoints.
     * Клиент будет отправлять этот токен в заголовке Authorization.
     */
    private String token;

    /**
     * Email аутентифицированного пользователя.
     */
    private String email;

    /**
     * Роль пользователя в системе (CLIENT, BARBER, ADMIN).
     * Определяет, к каким ресурсам у него есть доступ.
     */
    private String role;

    /**
     * Имя пользователя (необязательно, но удобно для приветствия).
     */
    private String firstName;

    /**
     * Фамилия пользователя.
     */
    private String lastName;
}