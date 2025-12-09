package com.example.barbershop.controller;

import com.example.barbershop.entity.User;
import com.example.barbershop.security.CustomUserDetails;
import com.example.barbershop.service.StatisticsService;
import com.example.barbershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final StatisticsService statisticsService;

    @Autowired
    public AdminController(UserService userService, StatisticsService statisticsService) {
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    /**
     * Панель администратора
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User admin = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Администратор не найден: " + email));

        // Проверяем, что пользователь действительно администратор
        if (!admin.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("Доступ запрещен: требуется роль ADMIN");
        }

        // Получаем статистику
        long totalUsers = userService.findAll().size();
        long clientsCount = userService.findAll().stream()
                .filter(u -> u.getRole().equals(User.Role.CLIENT))
                .count();
        long barbersCount = userService.findAll().stream()
                .filter(u -> u.getRole().equals(User.Role.BARBER))
                .count();
        long adminsCount = userService.findAll().stream()
                .filter(u -> u.getRole().equals(User.Role.ADMIN))
                .count();

        model.addAttribute("admin", admin);
        model.addAttribute("pageTitle", "Панель администратора");
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("clientsCount", clientsCount);
        model.addAttribute("barbersCount", barbersCount);
        model.addAttribute("adminsCount", adminsCount);

        return "admin/dashboard";
    }

    /**
     * Управление пользователями
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User admin = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

        List<User> allUsers = userService.findAll();

        model.addAttribute("admin", admin);
        model.addAttribute("pageTitle", "Управление пользователями");
        model.addAttribute("users", allUsers);

        return "admin/users";
    }

    /**
     * Управление услугами (перенаправление на существующий контроллер)
     */
    @GetMapping("/services")
    public String manageServices(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User admin = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

        model.addAttribute("admin", admin);
        model.addAttribute("pageTitle", "Управление услугами");

        // Перенаправляем на существующую страницу услуг
        return "redirect:/services";
    }

    /**
     * Статистика системы
     */
    @GetMapping("/statistics")
    public String statistics(Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || !userDetails.getUser().getRole().equals(User.Role.ADMIN)) {
            return "redirect:/";
        }

        // Получаем все виды статистики
        Map<String, Object> generalStats = statisticsService.getGeneralStatistics();
        Map<String, Object> weeklyStats = statisticsService.getWeeklyStatistics();
        Map<String, Object> barberStats = statisticsService.getBarberStatistics();
        Map<String, Object> serviceStats = statisticsService.getServiceStatistics();

        model.addAttribute("generalStats", generalStats);
        model.addAttribute("weeklyStats", weeklyStats);
        model.addAttribute("barberStats", barberStats);
        model.addAttribute("serviceStats", serviceStats);
        model.addAttribute("user", userDetails.getUser());
        model.addAttribute("pageTitle", "Статистика системы");

        return "admin/statistics";
    }

    /**
     * Настройки системы
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User admin = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Администратор не найден"));

        model.addAttribute("admin", admin);
        model.addAttribute("pageTitle", "Настройки системы");

        return "admin/settings";
    }
}