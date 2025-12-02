package com.example.barbershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для основных страниц приложения.
 */
@Controller
public class HomeController {

    /**
     * Главная страница приложения.
     */
    @GetMapping("/")
    public String home() {
        return "home"; // Будет искать шаблон src/main/resources/templates/home.html
    }

    /**
     * Страница регистрации нового пользователя.
     */
    @GetMapping("/register")
    public String register() {
        return "register"; // templates/register.html
    }

    /**
     * Страница "О проекте" / "Об авторе".
     */
    @GetMapping("/about")
    public String about() {
        return "about"; // templates/about.html
    }

    /**
     * Страница входа в систему.
     */
    @GetMapping("/login")
    public String login() {
        return "login"; // templates/login.html
    }

    @GetMapping("/api-test")
    public String apiTestPage() {
        return "api-test"; // Открывает templates/api-test.html
    }
}