package com.example.barbershop.repository;

import com.example.barbershop.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью Appointment (Запись на прием).
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Найти все записи клиента.
     */
    List<Appointment> findByClientId(Long clientId);

    /**
     * Найти все записи, связанные с перечисленными услугами мастера.
     */
    List<Appointment> findByBarberServiceIdIn(List<Long> barberServiceIds);

    /**
     * Найти записи по услуге мастера и времени.
     */
    List<Appointment> findByBarberServiceIdInAndAppointmentDateTimeBetween(
            List<Long> barberServiceIds,
            LocalDateTime start,
            LocalDateTime end
    );


    /**
     * Найти записи в указанном временном интервале.
     */
    List<Appointment> findByAppointmentDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Проверить, есть ли пересекающиеся записи у мастера.
     * Этот метод будет реализован в сервисе.
     */
    List<Appointment> findByBarberServiceId(Long barberServiceId);


}