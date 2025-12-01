package com.example.barbershop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Пользователь". Представляет всех пользователей системы:
 * клиентов, парикмахеров и администраторов.
 * Разделение реализовано через поле {@link User#role}.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id // Указывает, что это первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING) // Храним значение enum как строку в БД (CLIENT, BARBER, ADMIN)
    @Column(nullable = false)
    private Role role;


    /**
     * Роли пользователей в системе.
     * Используется для разграничения прав доступа.
     */
    public enum Role {
        CLIENT,    // Клиент парикмахерской
        BARBER,    // Парикмахер (мастер)
        ADMIN      // Администратор
    }
}