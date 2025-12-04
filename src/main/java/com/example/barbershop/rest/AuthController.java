package com.example.barbershop.rest;

import com.example.barbershop.dto.LoginRequest;
import com.example.barbershop.dto.AuthResponse;
import com.example.barbershop.security.CustomUserDetailsService;
import com.example.barbershop.security.JwtUtils;
import com.example.barbershop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.barbershop.dto.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    /**
     * Вход в систему с получением JWT токена.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 1. Аутентифицируем пользователя
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Загружаем UserDetails
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // 3. Генерируем JWT токен
        final String token = jwtUtils.generateToken(userDetails);

        // 4. Получаем полную информацию о пользователе из БД
        var user = userService.findByEmail(request.getEmail()).orElseThrow();

        // 5. Формируем ответ
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        return ResponseEntity.ok(response);
    }

    /**
     * Регистрация нового пользователя.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // 1. Создаем нового пользователя
        var newUser = new com.example.barbershop.entity.User();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // Пароль захешируется в сервисе
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setRole(com.example.barbershop.entity.User.Role.CLIENT); // По умолчанию клиент

        // 2. Сохраняем через UserService (там хеширование пароля)
        var savedUser = userService.registerUser(newUser);

        // 3. Генерируем токен для автоматического входа
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        final String token = jwtUtils.generateToken(userDetails);

        // 4. Формируем ответ
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());

        return ResponseEntity.ok(response);
    }

    /**
     * Тестовый endpoint (оставляем для обратной совместимости).
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API работает с JWT! Время: " + new java.util.Date());
    }
}