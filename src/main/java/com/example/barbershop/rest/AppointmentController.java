package com.example.barbershop.rest;

import com.example.barbershop.dto.AppointmentDto;
import com.example.barbershop.entity.Appointment;
import com.example.barbershop.entity.User;
import com.example.barbershop.service.AppointmentService;
import com.example.barbershop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления записями на прием.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService;

    /**
     * Получить все записи (только для администраторов).
     * GET /api/appointments
     */
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.findAll();
    }

    /**
     * Получить запись по ID.
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.findById(id);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Создать новую запись.
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentDto appointmentDto) {
        try {
            // Находим клиента по ID из DTO
            User client = userService.findById(appointmentDto.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            // Создаем запись через сервис
            Appointment appointment = appointmentService.createAppointment(
                    client,
                    appointmentDto.getBarberServiceId(),
                    appointmentDto.getAppointmentDateTime()
            );

            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка создания записи: " + e.getMessage());
        }
    }

    /**
     * Отменить запись.
     * PUT /api/appointments/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.cancelAppointment(id);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить записи клиента.
     * GET /api/appointments/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public List<Appointment> getClientAppointments(@PathVariable Long clientId) {
        return appointmentService.getClientAppointments(clientId);
    }

    /**
     * Получить записи мастера.
     * GET /api/appointments/barber/{barberId}
     */
    @GetMapping("/barber/{barberId}")
    public List<Appointment> getBarberAppointments(@PathVariable Long barberId) {
        return appointmentService.findAppointmentsByBarber(barberId);
    }
}