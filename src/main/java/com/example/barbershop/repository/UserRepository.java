package com.example.barbershop.repository;

import com.example.barbershop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью User (Пользователь).
 * Наследует стандартные CRUD-операции от JpaRepository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по email (уникальному).
     * Будет использоваться для аутентификации.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя с заданным email.
     */
    boolean existsByEmail(String email);

    long countByRole(User.Role role);

}