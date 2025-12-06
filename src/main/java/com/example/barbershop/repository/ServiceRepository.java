package com.example.barbershop.repository;

import com.example.barbershop.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью Service (Услуга).
 */
@Repository
public interface ServiceRepository extends JpaRepository<ServiceItem, Long> {
    // Дополнительные методы при необходимости
    // Например, поиск по имени:
    // List<Service> findByNameContainingIgnoreCase(String name);
}