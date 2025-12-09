package com.example.barbershop.repository;

import com.example.barbershop.entity.BarberService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы со связкой "Услуга мастера".
 */
@Repository
public interface BarberServiceRepository extends JpaRepository<BarberService, Long> {

    /**
     * Находит все услуги, предоставляемые конкретным мастером.
     */
    List<BarberService> findByBarberId(Long barberId);

    /**
     * Находит всех мастеров, оказывающих конкретную услугу.
     */
    List<BarberService> findByServiceId(Long serviceId);

    /**
     * Проверяет, существует ли уже связь мастер-услуга.
     */
    boolean existsByBarberIdAndServiceId(Long barberId, Long serviceId);
    long countByServiceId(Long serviceId);
}