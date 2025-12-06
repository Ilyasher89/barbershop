package com.example.barbershop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BarberServiceDto {
    private Long id;
    private Long barberId;
    private String barberName;
    private String barberSpecialization;
    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private BigDecimal actualPrice;
    private Integer actualDurationMinutes;
}