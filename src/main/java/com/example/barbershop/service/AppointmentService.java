package com.example.barbershop.service;

import com.example.barbershop.entity.Appointment;
import com.example.barbershop.entity.BarberService;
import com.example.barbershop.entity.User;
import com.example.barbershop.repository.AppointmentRepository;
import com.example.barbershop.repository.BarberServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для управления записями на прием.
 * Содержит бизнес-логику бронирования и проверки доступности.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberServiceRepository barberServiceRepository;

    /**
     * Создать новую запись на прием.
     */
    @Transactional
    public Appointment createAppointment(User client, Long barberServiceId, LocalDateTime dateTime) {
        // 1. Найти выбранную услугу мастера
        BarberService barberService = barberServiceRepository.findById(barberServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Услуга мастера не найдена"));

        // 2. Проверить, что время свободно (базовая проверка)
        if (!isTimeSlotAvailable(barberService.getBarber().getId(), dateTime,
                barberService.getActualDurationMinutes())) {
            throw new IllegalArgumentException("Выбранное время занято");
        }

        // 3. Создать запись
        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarberService(barberService);
        appointment.setAppointmentDateTime(dateTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    /**
     * Проверить доступность временного слота для мастера.
     * Упрощенная версия - проверяет только точное совпадение времени.
     */
    private boolean isTimeSlotAvailable(Long barberId, LocalDateTime startTime, Integer durationMinutes) {
        // Находим все записи мастера через BarberService
        List<BarberService> barberServices = barberServiceRepository.findByBarberId(barberId);

        // Для каждой услуги мастера проверяем его записи
        for (BarberService bs : barberServices) {
            // TODO: Реализовать более сложную логику проверки пересечений по времени
            // Пока просто проверяем, что в это время у мастера нет других записей
            List<Appointment> existingAppointments = appointmentRepository
                    .findByBarberServiceIdAndAppointmentDateTime(bs.getId(), startTime);

            if (!existingAppointments.isEmpty()) {
                return false; // Время занято
            }
        }
        return true; // Время свободно
    }

    /**
     * Найти все записи клиента.
     */
    public List<Appointment> getClientAppointments(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    /**
     * Отменить запись.
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Найти запись по ID.
     */
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));
    }
    /**
     * Найти все записи (приемы) конкретного мастера.
     */
    public List<Appointment> findAppointmentsByBarber(Long barberId) {
        // 1. Находим все услуги, которые предоставляет этот мастер
        List<BarberService> barberServices = barberServiceRepository.findByBarberId(barberId);

        // 2. Собираем ID всех этих услуг мастера
        List<Long> barberServiceIds = barberServices.stream()
                .map(BarberService::getId)
                .toList();

        // 3. Находим все записи, связанные с этими услугами мастера
        // Для этого добавим новый метод в AppointmentRepository
        return appointmentRepository.findByBarberServiceIdIn(barberServiceIds);
    }

    /**
     * Получить все записи.
     */
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }
}