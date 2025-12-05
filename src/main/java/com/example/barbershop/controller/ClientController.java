package com.example.barbershop.controller;

import com.example.barbershop.entity.User;
import com.example.barbershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UserService userService;

    @Autowired
    public ClientController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Личный кабинет клиента
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Способ 1: Получаем аутентификацию из SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Получаем email из аутентификации
        String email = authentication.getName();
        System.out.println("DEBUG: User email from authentication: " + email);

        // Находим пользователя в базе
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + email));

        System.out.println("DEBUG: Found user: " + user.getEmail());
        System.out.println("DEBUG: User firstName: " + user.getFirstName());

        // Добавляем данные пользователя в модель
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Личный кабинет клиента");

        return "client/dashboard";
    }

    /**
     * Профиль клиента (редактирование данных)
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Мой профиль");
        return "client/profile";
    }

    /**
     * История записей клиента
     */
    @GetMapping("/appointments")
    public String appointments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Мои записи");

        return "client/appointments";
    }
}