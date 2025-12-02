package com.example.barbershop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    private Long clientId;
    private Long barberServiceId;
    private LocalDateTime appointmentDateTime;
}