package com.example.barbershop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private Long barberServiceId;
    private LocalDateTime appointmentDateTime;
}