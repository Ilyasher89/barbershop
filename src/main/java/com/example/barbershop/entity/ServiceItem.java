package com.example.barbershop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Услуга". Описывает базовую услугу, предоставляемую парикмахерской.
 * Например: "Мужская стрижка", "Детская стрижка", "Окрашивание".
 */
@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Название услуги

    private String description; // Описание услуги

    @Column(name = "base_duration_minutes", nullable = false)
    private Integer baseDurationMinutes; // Базовая длительность в минутах

    @Column(name = "base_price", nullable = false)
    private Double basePrice; // Базовая стоимость услуги

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<BarberService> barberServices = new ArrayList<>();


}