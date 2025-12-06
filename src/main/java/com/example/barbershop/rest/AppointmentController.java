package com.example.barbershop.rest;

import com.example.barbershop.dto.AppointmentDto;
import com.example.barbershop.dto.AppointmentResponseDto; // <-- Добавить импорт
import com.example.barbershop.entity.*;
import com.example.barbershop.repository.BarberServiceRepository;
import com.example.barbershop.service.AppointmentService;
import com.example.barbershop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST контроллер для управления записями на прием.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final BarberServiceRepository barberServiceRepository;
    private final AppointmentService appointmentService;
    private final UserService userService;

    /**
     * Получить все записи (только для администраторов).
     * GET /api/appointments
     */
    @GetMapping
    public List<AppointmentResponseDto> getAllAppointments() { // <-- Изменить тип возврата
        return appointmentService.getAllAppointmentsAsDto(); // <-- Использовать DTO метод
    }

    /**
     * Получить запись по ID.
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) { // <-- Изменить тип
        try {
            Appointment appointment = appointmentService.findById(id);
            AppointmentResponseDto dto = convertToDto(appointment); // <-- Конвертировать в DTO
            return ResponseEntity.ok(dto);
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
            User client = userService.findById(appointmentDto.getClientId())
                    .orElseThrow(() -> new IllegalArgumentException("Клиент не найден"));

            Appointment appointment = appointmentService.createAppointment(
                    client,
                    appointmentDto.getBarberServiceId(),
                    appointmentDto.getAppointmentDateTime()
            );

            // ВОТ ИСПРАВЛЕНИЕ: возвращаем DTO, а не Entity
            AppointmentResponseDto responseDto = convertToDto(appointment);
            return ResponseEntity.ok(responseDto);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка создания записи: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(errorResponse);
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
            AppointmentResponseDto responseDto = convertToDto(appointment); // <-- Конвертировать в DTO
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Получить записи клиента.
     * GET /api/appointments/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public List<AppointmentResponseDto> getClientAppointments(@PathVariable Long clientId) { // <-- Изменить тип
        List<Appointment> appointments = appointmentService.getClientAppointments(clientId);
        return appointments.stream()
                .map(this::convertToDto) // <-- Конвертировать каждую запись
                .toList();
    }

    /**
     * Получить записи мастера.
     * GET /api/appointments/barber/{barberId}
     */
    @GetMapping("/barber/{barberId}")
    public List<AppointmentResponseDto> getBarberAppointments(@PathVariable Long barberId) { // <-- Изменить тип
        List<Appointment> appointments = appointmentService.findAppointmentsByBarber(barberId);
        return appointments.stream()
                .map(this::convertToDto) // <-- Конвертировать каждую запись
                .toList();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Конвертирует Appointment entity в AppointmentResponseDto.
     * (Дублирует логику из сервиса, но можно вынести в утилитный класс)
     */
    private AppointmentResponseDto convertToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();

        // Основные поля
        dto.setId(appointment.getId());
        dto.setAppointmentDateTime(appointment.getAppointmentDateTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setCreatedAt(appointment.getCreatedAt());

        // Информация о клиенте
        if (appointment.getClient() != null) {
            User client = appointment.getClient();
            dto.setClientId(client.getId());
            dto.setClientEmail(client.getEmail());

            // Объединяем имя и фамилию
            String clientFullName = "";
            if (client.getFirstName() != null) {
                clientFullName += client.getFirstName();
            }
            if (client.getLastName() != null) {
                if (!clientFullName.isEmpty()) {
                    clientFullName += " ";
                }
                clientFullName += client.getLastName();
            }
            dto.setClientName(clientFullName.isEmpty() ? client.getEmail() : clientFullName);
        }

        // Информация об услуге и мастере
        if (appointment.getBarberService() != null) {
            BarberService barberService = appointment.getBarberService();

            // Услуга
            if (barberService.getService() != null) {
                ServiceItem service = barberService.getService();
                dto.setServiceId(service.getId());
                dto.setServiceName(service.getName());
                dto.setServicePrice(service.getBasePrice());
            }

            // Мастер
            if (barberService.getBarber() != null) {
                Barber barber = barberService.getBarber();
                dto.setBarberId(barber.getId());

                if (barber.getUser() != null) {
                    User barberUser = barber.getUser();
                    String barberFullName = "";
                    if (barberUser.getFirstName() != null) {
                        barberFullName += barberUser.getFirstName();
                    }
                    if (barberUser.getLastName() != null) {
                        if (!barberFullName.isEmpty()) {
                            barberFullName += " ";
                        }
                        barberFullName += barberUser.getLastName();
                    }
                    dto.setBarberName(barberFullName.isEmpty() ? barberUser.getEmail() : barberFullName);
                }
            }
        }

        return dto;
    }

    /**
     * Тестовый эндпоинт для проверки данных.
     */
    @GetMapping("/test-data")
    public ResponseEntity<?> getTestData() {
        try {
            long barberServicesCount = barberServiceRepository.count();
            long usersCount = userService.findAll().size();

            Map<String, Object> data = new HashMap<>();
            data.put("barberServicesCount", barberServicesCount);
            data.put("usersCount", usersCount);
            data.put("message", "Данные для теста");

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }
}