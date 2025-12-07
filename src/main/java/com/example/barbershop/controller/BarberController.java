package com.example.barbershop.controller;

import com.example.barbershop.dto.AppointmentResponseDto;
import com.example.barbershop.entity.Appointment;
import com.example.barbershop.entity.Barber;
import com.example.barbershop.entity.User;
import com.example.barbershop.repository.BarberRepository;
import com.example.barbershop.security.CustomUserDetails;
import com.example.barbershop.service.AppointmentService;
import com.example.barbershop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/barber")
@RequiredArgsConstructor
@Slf4j
public class BarberController {

    private final UserService userService;
    private final AppointmentService appointmentService;
    private final BarberRepository barberRepository;

    /**
     * Панель парикмахера (мастера)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User barberUser = userDetails.getUser();

        // Проверяем, что пользователь действительно мастер
        if (!barberUser.getRole().equals(User.Role.BARBER)) {
            log.warn("User {} attempted to access barber dashboard without BARBER role", barberUser.getEmail());
            return "redirect:/client/dashboard";
        }

        model.addAttribute("barber", barberUser);
        model.addAttribute("pageTitle", "Панель парикмахера");

        return "barber/dashboard";
    }

    /**
     * Расписание мастера
     */
    @GetMapping("/schedule")
    public String schedule(Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User barberUser = userDetails.getUser();

        if (!barberUser.getRole().equals(User.Role.BARBER)) {
            return "redirect:/client/dashboard";
        }

        model.addAttribute("barber", barberUser);
        model.addAttribute("pageTitle", "Мое расписание");

        return "barber/schedule";
    }

    /**
     * Записи клиентов на мастера
     */
    @GetMapping("/appointments")
    public String appointments(Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User barberUser = userDetails.getUser();

        if (!barberUser.getRole().equals(User.Role.BARBER)) {
            return "redirect:/client/dashboard";
        }

        // Находим сущность Barber по User
        Barber barber = barberRepository.findByUserId(barberUser.getId())
                .orElseThrow(() -> new RuntimeException("Мастер не найден в системе"));

        // Получаем записи на этого мастера
        List<Appointment> appointments = appointmentService.findAppointmentsByBarber(barber.getId());

        // Конвертируем в DTO для отображения
        List<AppointmentResponseDto> appointmentDtos = appointments.stream()
                .map(appointmentService::convertToDto)
                .collect(Collectors.toList());

        log.info("Barber {} has {} appointments", barberUser.getEmail(), appointmentDtos.size());

        model.addAttribute("barber", barberUser);
        model.addAttribute("barberEntity", barber); // Сущность Barber для деталей
        model.addAttribute("appointments", appointmentDtos);
        model.addAttribute("pageTitle", "Записи клиентов");

        return "barber/appointments";
    }

    /**
     * Профиль мастера
     */
    @GetMapping("/profile")
    public String profile(Model model,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User barberUser = userDetails.getUser();

        if (!barberUser.getRole().equals(User.Role.BARBER)) {
            return "redirect:/client/profile";
        }

        model.addAttribute("barber", barberUser);
        model.addAttribute("pageTitle", "Мой профиль");

        return "barber/profile";
    }
}