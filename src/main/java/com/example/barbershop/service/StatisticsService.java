package com.example.barbershop.service;

import com.example.barbershop.entity.*;
import com.example.barbershop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatisticsService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;
    private final BarberServiceRepository barberServiceRepository;

    /**
     * Общая статистика системы
     */
    public Map<String, Object> getGeneralStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Количество пользователей по ролям
        long totalUsers = userRepository.count();
        long clients = userRepository.countByRole(User.Role.CLIENT);
        long barbers = userRepository.countByRole(User.Role.BARBER);
        long admins = userRepository.countByRole(User.Role.ADMIN);

        // Количество записей
        long totalAppointments = appointmentRepository.count();
        long scheduledAppointments = appointmentRepository.countByStatus(Appointment.AppointmentStatus.SCHEDULED);
        long completedAppointments = appointmentRepository.countByStatus(Appointment.AppointmentStatus.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);

        // Количество услуг
        long totalServices = serviceRepository.count();
        long totalBarberServices = barberServiceRepository.count();

        // Выручка (только завершенные записи)
        Double totalRevenue = appointmentRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = 0.0;

        // Средний чек
        Double averageCheck = completedAppointments > 0 ? totalRevenue / completedAppointments : 0.0;

        stats.put("totalUsers", totalUsers);
        stats.put("clients", clients);
        stats.put("barbers", barbers);
        stats.put("admins", admins);
        stats.put("totalAppointments", totalAppointments);
        stats.put("scheduledAppointments", scheduledAppointments);
        stats.put("completedAppointments", completedAppointments);
        stats.put("cancelledAppointments", cancelledAppointments);
        stats.put("totalServices", totalServices);
        stats.put("totalBarberServices", totalBarberServices);
        stats.put("totalRevenue", String.format("%.2f ₽", totalRevenue));
        stats.put("averageCheck", String.format("%.2f ₽", averageCheck));

        log.info("Собрана общая статистика: {} пользователей, {} записей", totalUsers, totalAppointments);
        return stats;
    }

    /**
     * Статистика за последние 7 дней
     */
    public Map<String, Object> getWeeklyStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Новые пользователи за неделю
        long totalUsers = userRepository.count();
        long newUsers = totalUsers;

        // Новые записи за неделю
        long newAppointments = appointmentRepository.countByCreatedAtAfter(weekAgo);

        // Выручка за неделю
        Double weeklyRevenue = appointmentRepository.calculateRevenueAfterDate(weekAgo);
        if (weeklyRevenue == null) weeklyRevenue = 0.0;

        // Самые популярные услуги

        List<Object[]> popularServices = appointmentRepository.findPopularServicesLast7Days(weekAgo);

        stats.put("newUsers", newUsers);
        stats.put("newAppointments", newAppointments);
        stats.put("weeklyRevenue", String.format("%.2f ₽", weeklyRevenue));
        stats.put("popularServices", popularServices);

        return stats;
    }

    /**
     * Статистика по мастерам
     */
    public Map<String, Object> getBarberStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Barber> allBarbers = barberRepository.findAll();
        stats.put("totalBarbers", allBarbers.size());

        // Собираем статистику по каждому мастеру
        List<Map<String, Object>> barberStats = allBarbers.stream().map(barber -> {
            Map<String, Object> barberData = new HashMap<>();

            // Количество записей мастера
            long barberAppointments = appointmentRepository.countByBarber(barber.getId());

            // Выручка мастера
            Double barberRevenue = appointmentRepository.calculateRevenueByBarber(barber.getId());
            if (barberRevenue == null) barberRevenue = 0.0;

            // Средняя оценка (можно добавить позже)

            barberData.put("id", barber.getId());
            barberData.put("name", barber.getUser().getFirstName() + " " + barber.getUser().getLastName());
            barberData.put("email", barber.getUser().getEmail());
            barberData.put("specialization", barber.getSpecialization());
            barberData.put("appointmentsCount", barberAppointments);
            barberData.put("revenue", String.format("%.2f ₽", barberRevenue));

            return barberData;
        }).toList();

        stats.put("barbers", barberStats);
        return stats;
    }

    /**
     * Статистика по услугам
     */
    public Map<String, Object> getServiceStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<ServiceItem> allServices = serviceRepository.findAll();
        stats.put("totalServices", allServices.size());

        // Статистика по каждой услуге
        List<Map<String, Object>> serviceStats = allServices.stream().map(service -> {
            Map<String, Object> serviceData = new HashMap<>();

            // Количество записей на эту услугу
            long serviceAppointments = appointmentRepository.countByService(service.getId());

            // Количество мастеров, предоставляющих услугу
            long barbersCount = barberServiceRepository.countByServiceId(service.getId());

            serviceData.put("id", service.getId());
            serviceData.put("name", service.getName());
            serviceData.put("price", service.getBasePrice());
            serviceData.put("duration", service.getBaseDurationMinutes());
            serviceData.put("appointmentsCount", serviceAppointments);
            serviceData.put("barbersCount", barbersCount);

            return serviceData;
        }).toList();

        stats.put("services", serviceStats);
        return stats;
    }
}