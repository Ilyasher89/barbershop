package com.example.barbershop.service;

import com.example.barbershop.entity.Barber;
import com.example.barbershop.entity.Service;
import com.example.barbershop.entity.BarberService;
import com.example.barbershop.entity.User;
import com.example.barbershop.repository.BarberRepository;
import com.example.barbershop.repository.ServiceRepository;
import com.example.barbershop.repository.BarberServiceRepository;
import com.example.barbershop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления парикмахерами и их услугами.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarberManagementService {

    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final BarberServiceRepository barberServiceRepository;
    private final UserRepository userRepository;

    /**
     * Получить всех парикмахеров.
     */
    public List<Barber> getAllBarbers() {
        return barberRepository.findAll();
    }

    /**
     * Найти парикмахера по ID.
     */
    public Barber getBarberById(Long id) {
        return barberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Парикмахер не найден"));
    }

    /**
     * Создать нового парикмахера на основе пользователя.
     */
    @Transactional
    public Barber createBarber(Long userId, String specialization) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем, что пользователь имеет роль BARBER
        if (user.getRole() != User.Role.BARBER) {
            throw new IllegalArgumentException("Пользователь не является парикмахером");
        }

        // Проверяем, что у пользователя еще нет записи парикмахера
        if (barberRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("Парикмахер уже существует для этого пользователя");
        }

        Barber barber = new Barber();
        barber.setUser(user);
        barber.setSpecialization(specialization);

        return barberRepository.save(barber);
    }

    /**
     * Добавить услугу парикмахеру.
     */
    @Transactional
    public BarberService addServiceToBarber(Long barberId, Long serviceId,
                                            Double actualPrice, Integer actualDurationMinutes) {
        Barber barber = getBarberById(barberId);
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));

        // Проверяем, не добавлена ли уже эта услуга
        if (barberServiceRepository.existsByBarberIdAndServiceId(barberId, serviceId)) {
            throw new IllegalArgumentException("Эта услуга уже добавлена парикмахеру");
        }

        BarberService barberService = new BarberService();
        barberService.setBarber(barber);
        barberService.setService(service);
        barberService.setActualPrice(actualPrice != null ? actualPrice : service.getBasePrice());
        barberService.setActualDurationMinutes(actualDurationMinutes != null ?
                actualDurationMinutes : service.getBaseDurationMinutes());

        return barberServiceRepository.save(barberService);
    }

    /**
     * Получить все услуги конкретного парикмахера.
     */
    public List<BarberService> getBarberServices(Long barberId) {
        return barberServiceRepository.findByBarberId(barberId);
    }

    /**
     * Получить всех парикмахеров, оказывающих конкретную услугу.
     */
    public List<BarberService> getBarbersByService(Long serviceId) {
        return barberServiceRepository.findByServiceId(serviceId);
    }

    /**
     * Удалить услугу у парикмахера.
     */
    @Transactional
    public void removeServiceFromBarber(Long barberServiceId) {
        if (!barberServiceRepository.existsById(barberServiceId)) {
            throw new IllegalArgumentException("Связь парикмахер-услуга не найдена");
        }
        barberServiceRepository.deleteById(barberServiceId);
    }
}