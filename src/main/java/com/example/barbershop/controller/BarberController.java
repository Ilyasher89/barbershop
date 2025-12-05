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
@RequestMapping("/barber")
public class BarberController {

    private final UserService userService;

    @Autowired
    public BarberController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Панель парикмахера (мастера)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User barber = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Мастер не найден: " + email));

        // Проверяем, что пользователь действительно мастер
        if (!barber.getRole().equals(User.Role.BARBER)) {
            throw new RuntimeException("Доступ запрещен: требуется роль BARBER");
        }

        model.addAttribute("barber", barber);
        model.addAttribute("pageTitle", "Панель парикмахера");

        return "barber/dashboard";
    }

    /**
     * Расписание мастера
     */
    @GetMapping("/schedule")
    public String schedule(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User barber = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Мастер не найден"));

        model.addAttribute("barber", barber);
        model.addAttribute("pageTitle", "Мое расписание");

        return "barber/schedule";
    }

    /**
     * Записи клиентов на сегодня/завтра
     */
    @GetMapping("/appointments")
    public String appointments(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User barber = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Мастер не найден"));

        model.addAttribute("barber", barber);
        model.addAttribute("pageTitle", "Записи клиентов");

        return "barber/appointments";
    }

    /**
     * Профиль мастера
     */
    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User barber = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Мастер не найден"));

        model.addAttribute("barber", barber);
        model.addAttribute("pageTitle", "Мой профиль");

        return "barber/profile";
    }
}