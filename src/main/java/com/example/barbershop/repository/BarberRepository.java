package com.example.barbershop.repository;

import com.example.barbershop.entity.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Barber (Парикмахер).
 */
@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {

    /**
     * Найти парикмахера по связанному пользователю (user_id).
     */
    Optional<Barber> findByUserId(Long userId);

    /**
     * Найти всех парикмахеров по специализации.
     */
    List<Barber> findBySpecializationContainingIgnoreCase(String specialization);
}