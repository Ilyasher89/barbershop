package com.example.barbershop.controller;

import com.example.barbershop.dto.AppointmentRequest;
import com.example.barbershop.dto.AppointmentResponseDto;
import com.example.barbershop.entity.Appointment;
import com.example.barbershop.entity.Barber;
import com.example.barbershop.entity.User;
import com.example.barbershop.repository.BarberRepository;
import com.example.barbershop.security.CustomUserDetails;
import com.example.barbershop.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final AppointmentService appointmentService;
    private final BarberRepository barberRepository;

    @GetMapping("/dashboard")
    public String clientDashboard(Model model,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Unauthorized access to /client/dashboard");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        log.info("Client dashboard accessed by user: {} (ID: {})", user.getEmail(), user.getId());

        model.addAttribute("pageTitle", "Личный кабинет клиента");
        model.addAttribute("user", user);
        return "client/dashboard";
    }

    @GetMapping("/appointments")
    public String clientAppointments(Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        System.out.println("DEBUG: User ID = " + user.getId());

        // Получаем записи
        List<Appointment> appointments = appointmentService.getClientAppointments(user.getId());
        System.out.println("DEBUG: Found " + appointments.size() + " appointments");

        // Конвертируем в DTO
        List<AppointmentResponseDto> appointmentDtos = new ArrayList<>();
        for (Appointment appointment : appointments) {
            appointmentDtos.add(appointmentService.convertToDto(appointment));
        }

        model.addAttribute("appointments", appointmentDtos);
        model.addAttribute("user", user);

        return "client/appointments";
    }

    @GetMapping("/profile")
    public String clientProfile(Model model,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.warn("Unauthorized access to /client/profile");
            return "redirect:/login";
        }

        User user = userDetails.getUser();
        log.info("Client profile accessed by user: {} (ID: {})", user.getEmail(), user.getId());

        model.addAttribute("pageTitle", "Мой профиль");
        model.addAttribute("user", user);
        return "client/profile";
    }
    @GetMapping("/appointments/new")
    public String newAppointmentForm(Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        // Получаем всех мастеров для выпадающего списка
        List<Barber> barbers = barberRepository.findAll();

        model.addAttribute("barbers", barbers);
        model.addAttribute("user", userDetails.getUser());

        // Пустой объект для формы
        model.addAttribute("appointmentRequest", new AppointmentRequest());

        return "client/new-appointment";
    }

    @PostMapping("/appointments/new")
    public String createAppointment(@ModelAttribute AppointmentRequest request,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        try {
            // Создаем запись через сервис
            appointmentService.createAppointment(
                    userDetails.getUser(),
                    request.getBarberServiceId(),
                    request.getAppointmentDateTime()
            );

            return "redirect:/client/appointments?success=true";
        } catch (Exception e) {
            return "redirect:/client/appointments/new?error=" + e.getMessage();
        }
    }

    /**
     * Для отладки: проверка передачи данных
     */
    @GetMapping("/debug")
    public String debugPage(Model model,
                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userDetails.getUser();

        // Тестовые данные
        model.addAttribute("testString", "Hello from Thymeleaf!");
        model.addAttribute("testNumber", 12345);
        model.addAttribute("testUserId", user.getId());
        model.addAttribute("user", user);

        return "client/debug";
    }
}