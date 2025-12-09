package com.example.barbershop.repository;

import com.example.barbershop.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    long countByStatus(Appointment.AppointmentStatus status);
    long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT COALESCE(SUM(bs.actualPrice), 0) FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE a.status = 'COMPLETED'")
    Double calculateTotalRevenue();

    @Query("SELECT COALESCE(SUM(bs.actualPrice), 0) FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE a.status = 'COMPLETED' AND a.createdAt >= :date")
    Double calculateRevenueAfterDate(@Param("date") LocalDateTime date);

    @Query("SELECT COALESCE(SUM(bs.actualPrice), 0) FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE a.status = 'COMPLETED' AND bs.barber.id = :barberId")
    Double calculateRevenueByBarber(@Param("barberId") Long barberId);

    @Query("SELECT bs.service.name, COUNT(a) as count FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE a.createdAt >= :date " +
            "GROUP BY bs.service.name " +
            "ORDER BY count DESC")
    List<Object[]> findPopularServicesLast7Days(@Param("date") LocalDateTime date);

    // Вспомогательные методы для подсчета
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE bs.barber.id = :barberId")
    long countByBarber(@Param("barberId") Long barberId);

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "JOIN a.barberService bs " +
            "WHERE bs.service.id = :serviceId")
    long countByService(@Param("serviceId") Long serviceId);


}