package com.example.barbershop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность "Запись на прием".
 * Связывает клиента, выбранную услугу мастера и временной слот.
 */
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Клиент, который записался на прием.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    /**
     * Конкретное предложение услуги от мастера.
     * Определяет: какого мастера, на какую услугу и по какой цене.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_service_id", nullable = false)
    private BarberService barberService;

    /**
     * Дата и время начала записи.
     */
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    /**
     * Статус записи.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    /**
     * Дата и время создания записи в системе.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Возможные статусы записи.
     */
    public enum AppointmentStatus {
        SCHEDULED,    // Запланирована
        COMPLETED,    // Завершена
        CANCELLED,    // Отменена
        NO_SHOW       // Клиент не явился
    }
}