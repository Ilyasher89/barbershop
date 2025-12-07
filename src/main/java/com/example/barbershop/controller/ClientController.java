package com.example.barbershop.controller;

import com.example.barbershop.dto.AppointmentRequest;
import com.example.barbershop.dto.AppointmentResponseDto;
import com.example.barbershop.entity.Appointment;
import com.example.barbershop.entity.Barber;
import com.example.barbershop.entity.BarberService;
import com.example.barbershop.entity.User;
import com.example.barbershop.repository.AppointmentRepository;
import com.example.barbershop.repository.BarberRepository;
import com.example.barbershop.repository.BarberServiceRepository;
import com.example.barbershop.security.CustomUserDetails;
import com.example.barbershop.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final AppointmentRepository appointmentRepository;
    private final BarberServiceRepository barberServiceRepository;

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
    public String newAppointmentForm(@RequestParam(required = false) Long serviceId,
                                     Model model,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        List<Barber> barbers = barberRepository.findAll();

        // Если передан serviceId, отмечаем его в модели
        model.addAttribute("selectedServiceId", serviceId);
        model.addAttribute("barbers", barbers);
        model.addAttribute("user", userDetails.getUser());
        model.addAttribute("appointmentRequest", new AppointmentRequest());

        return "client/new-appointment";
    }
    @GetMapping("/api/occupied-slots")
    @ResponseBody
    public List<String> getOccupiedSlots(@RequestParam Long barberServiceId,
                                         @RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            LocalDateTime dayStart = localDate.atStartOfDay();
            LocalDateTime dayEnd = localDate.plusDays(1).atStartOfDay();

            // 1. Найти выбранную услугу мастера
            BarberService barberService = barberServiceRepository.findById(barberServiceId)
                    .orElseThrow(() -> new RuntimeException("Услуга не найдена"));

            // 2. Найти все услуги ЭТОГО ЖЕ МАСТЕРА
            Long barberId = barberService.getBarber().getId();
            List<BarberService> allBarberServices = barberServiceRepository.findByBarberId(barberId);
            List<Long> allBarberServiceIds = allBarberServices.stream()
                    .map(BarberService::getId)
                    .collect(Collectors.toList());

            // 3. Найти все записи этого мастера на эту дату
            List<Appointment> appointments = appointmentRepository
                    .findByBarberServiceIdInAndAppointmentDateTimeBetween(
                            allBarberServiceIds,
                            dayStart,
                            dayEnd
                    );

            // 4. Сгенерировать список ЗАНЯТЫХ 15-минутных слотов
            List<String> occupiedSlots = new ArrayList<>();

            for (Appointment appointment : appointments) {
                if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                    continue; // Пропускаем отмененные
                }

                LocalDateTime start = appointment.getAppointmentDateTime();
                Integer duration = appointment.getBarberService().getActualDurationMinutes();
                LocalDateTime end = start.plusMinutes(duration);

                // Генерируем все 15-минутные слоты в интервале
                LocalDateTime slot = start;
                while (slot.isBefore(end)) {
                    // Форматируем как "HH:mm"
                    String timeSlot = slot.toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"));
                    occupiedSlots.add(timeSlot);

                    // Следующий 15-минутный слот
                    slot = slot.plusMinutes(15);
                }
            }

            // Убираем дубликаты
            return occupiedSlots.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Ошибка при получении занятых слотов", e);
            return Collections.emptyList();
        }
    }

    @PostMapping("/appointments/new")
    public String createAppointment(@ModelAttribute AppointmentRequest request,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {
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
            // В случае ошибки возвращаемся на форму с сообщением
            log.error("Ошибка при создании записи: {}", e.getMessage());

            // Заново загружаем мастеров для формы
            List<Barber> barbers = barberRepository.findAll();
            model.addAttribute("barbers", barbers);
            model.addAttribute("user", userDetails.getUser());
            model.addAttribute("appointmentRequest", request);
            model.addAttribute("errorMessage", "Ошибка: " + e.getMessage());

            return "client/new-appointment";
        }
    }
    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        try {
            // Проверяем, что запись принадлежит клиенту
            Appointment appointment = appointmentService.findById(id);
            if (!appointment.getClient().getId().equals(userDetails.getUser().getId())) {
                throw new SecurityException("Нельзя отменить чужую запись");
            }

            appointmentService.cancelAppointment(id);
            return "redirect:/client/appointments?success=cancel";
        } catch (Exception e) {
            log.error("Ошибка отмены записи: {}", e.getMessage());
            return "redirect:/client/appointments?error=" + e.getMessage();
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