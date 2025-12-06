package com.example.barbershop.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Промежуточная сущность "Предложение услуги мастером".
 * Заменяет прямую связь ManyToMany между Barber и Service
 * и позволяет хранить индивидуальные атрибуты (цену, длительность)
 * для каждой конкретной пары "Мастер-Услуга".
 */
@Entity
@Table(name = "barber_services",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barber_id", "service_id"}))
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BarberService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Парикмахер, оказывающий данную услугу.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    /**
     * Конкретная услуга из справочника.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem service;

    /**
     * Фактическая цена на эту услугу у данного мастера.
     * Может отличаться от базовой цены в Service.basePrice.
     */
    @Column(name = "actual_price", nullable = false)
    private Double actualPrice;

    /**
     * Фактическая длительность услуги у данного мастера в минутах.
     * Может отличаться от базовой длительности в Service.baseDurationMinutes.
     */
    @Column(name = "actual_duration_minutes", nullable = false)
    private Integer actualDurationMinutes;

    // Конструктор для удобного создания
    public BarberService(Barber barber, ServiceItem service, Double actualPrice, Integer actualDurationMinutes) {
        this.barber = barber;
        this.service = service;
        this.actualPrice = actualPrice;
        this.actualDurationMinutes = actualDurationMinutes;
    }
}