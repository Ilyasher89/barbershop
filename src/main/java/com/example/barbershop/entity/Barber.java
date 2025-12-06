package com.example.barbershop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Парикмахер". Расширяет информацию о пользователе с ролью BARBER.
 * Содержит специфичные для мастера данные.
 */
@Entity
@Table(name = "barbers")
@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Связь с основным пользователем системы.
     * Один пользователь (с ролью BARBER) -> одна запись парикмахера.
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @ToString.Exclude // Чтобы избежать циклического вызова toString
    private User user;

    private String specialization; // Специализация: "Мужские стрижки", "Детские" и т.д.

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY)
    @ToString.Exclude // Чтобы не было бесконечной строки в toString()
    @JsonIgnore // Чтобы не сериализовать в JSON
    private List<BarberService> barberServices = new ArrayList<>();
}
