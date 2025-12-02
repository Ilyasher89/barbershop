package com.example.barbershop.rest;

import com.example.barbershop.dto.LoginRequest;
import com.example.barbershop.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для аутентификации и регистрации.
 * Обрабатывает запросы по пути /api/auth
 *
 * @RestController - указывает, что этот класс обрабатывает REST запросы
 * @RequestMapping("/api/auth") - все методы будут доступны по пути /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Обрабатывает POST запрос для входа в систему.
     *
     * @param loginRequest данные для входа (email и пароль) в формате JSON
     * @return ResponseEntity<AuthResponse> - ответ с токеном и информацией о пользователе
     *
     * @PostMapping("/login") - метод обрабатывает POST запросы на /api/auth/login
     * @RequestBody - параметр берется из тела HTTP-запроса (JSON)
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        // TODO: В будущем здесь будет реальная проверка логина/пароля
        // и генерация JWT токена

        System.out.println("Получен запрос на вход: email=" + loginRequest.getEmail());

        // Создаем заглушку ответа
        AuthResponse response = new AuthResponse();
        response.setEmail(loginRequest.getEmail());
        response.setRole("CLIENT"); // Временная заглушка
        response.setFirstName("Тестовый");
        response.setLastName("Пользователь");
        response.setToken("dummy-jwt-token-" + System.currentTimeMillis());

        // Возвращаем ответ с HTTP статусом 200 (OK)
        return ResponseEntity.ok(response);
    }

    /**
     * Обрабатывает POST запрос для регистрации нового пользователя.
     *
     * @param loginRequest данные для регистрации
     * @return ResponseEntity с сообщением об успехе
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest loginRequest) {
        // TODO: В будущем здесь будет реальная регистрация

        System.out.println("Получен запрос на регистрацию: email=" + loginRequest.getEmail());

        // Временная заглушка
        return ResponseEntity.ok("Регистрация успешна (заглушка) для: " + loginRequest.getEmail());
    }

    /**
     * Тестовый GET endpoint для проверки работы API.
     * Доступен по GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API работает! Время: " + new java.util.Date());
    }
}