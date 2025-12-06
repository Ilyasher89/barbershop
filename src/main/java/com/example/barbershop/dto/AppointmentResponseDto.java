package com.example.barbershop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDto {
    private Long id;
    private LocalDateTime appointmentDateTime;
    private String status;

    // Информация о клиенте
    private Long clientId;
    private String clientName;
    private String clientEmail;

    // Информация об услуге
    private Long serviceId;
    private String serviceName;
    private Double servicePrice;

    // Информация о мастере
    private Long barberId;
    private String barberName;

    // Время создания записи
    private LocalDateTime createdAt;
}