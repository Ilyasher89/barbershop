package com.example.barbershop.service;

import com.example.barbershop.entity.*;
import com.example.barbershop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.barbershop.dto.AppointmentResponseDto;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BarberServiceRepository barberServiceRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;

    /**
     * Создать новую запись на прием.
     */
    @Transactional
    public Appointment createAppointment(User client, Long barberServiceId, LocalDateTime dateTime) {
        BarberService barberService = barberServiceRepository.findById(barberServiceId)
                .orElseThrow(() -> new IllegalArgumentException("Услуга мастера не найдена"));

        if (!isTimeSlotAvailable(barberService.getBarber().getId(), dateTime,
                barberService.getActualDurationMinutes())) {
            throw new IllegalArgumentException("Выбранное время занято");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarberService(barberService);
        appointment.setAppointmentDateTime(dateTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);

        return appointmentRepository.save(appointment);
    }

    /**
     * Проверить доступность временного слота для мастера.
     */
    private boolean isTimeSlotAvailable(Long barberId, LocalDateTime startTime, Integer durationMinutes) {
        List<BarberService> barberServices = barberServiceRepository.findByBarberId(barberId);

        for (BarberService bs : barberServices) {
            List<Appointment> existingAppointments = appointmentRepository
                    .findByBarberServiceIdAndAppointmentDateTime(bs.getId(), startTime);

            if (!existingAppointments.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Создать тестовые услуги мастеров.
     */
    @Transactional
    public void createTestBarberServices() {
        if (barberServiceRepository.count() == 0) {
            System.out.println("=== СОЗДАНИЕ ТЕСТОВЫХ УСЛУГ МАСТЕРОВ ===");

            // 1. Найти или создать услуги
            if (serviceRepository.count() == 0) {
                ServiceItem haircut = new ServiceItem();
                haircut.setName("Мужская стрижка");
                haircut.setDescription("Классическая мужская стрижка");
                haircut.setBaseDurationMinutes(45);
                haircut.setBasePrice(1500.0);
                serviceRepository.save(haircut);

                ServiceItem beard = new ServiceItem();
                beard.setName("Уход за бородой");
                beard.setDescription("Стрижка и укладка бороды");
                beard.setBaseDurationMinutes(30);
                beard.setBasePrice(800.0);
                serviceRepository.save(beard);

                ServiceItem complex = new ServiceItem();
                complex.setName("Стрижка + Борода");
                complex.setDescription("Комплексная услуга");
                complex.setBaseDurationMinutes(75);
                complex.setBasePrice(2000.0);
                serviceRepository.save(complex);

                System.out.println("Создано 3 услуги");
            }

            // 2. Найти мастера (User) и связанную сущность Barber
            User barberUser = userRepository.findByEmail("barber@test.ru")
                    .orElseThrow(() -> new RuntimeException("Мастер barber@test.ru не найден"));

            // 3. Создать сущность Barber если её нет
            Barber barber = barberRepository.findByUserId(barberUser.getId())
                    .orElseGet(() -> {
                        Barber newBarber = new Barber();
                        newBarber.setUser(barberUser);
                        newBarber.setSpecialization("Мужские стрижки");
                        return barberRepository.save(newBarber);
                    });

            // 4. Создать связи мастер-услуга
            List<ServiceItem> allServices = serviceRepository.findAll();

            for (ServiceItem service : allServices) {
                if (!barberServiceRepository.existsByBarberIdAndServiceId(barber.getId(), service.getId())) {
                    BarberService bs = new BarberService();
                    bs.setBarber(barber);
                    bs.setService(service);
                    bs.setActualPrice(service.getBasePrice()); // Используем базовую цену
                    bs.setActualDurationMinutes(service.getBaseDurationMinutes()); // Используем базовую длительность
                    barberServiceRepository.save(bs);

                    System.out.println("Создана связь: " + barber.getUser().getFirstName() + " - " + service.getName() + " (ID: " + bs.getId() + ")");
                }
            }

            System.out.println("=== ГОТОВО: " + barberServiceRepository.count() + " услуг мастеров ===");
        }
    }

    /**
     * Найти все записи клиента.
     */
    public List<Appointment> getClientAppointments(Long clientId) {
        return appointmentRepository.findByClientId(clientId);
    }

    /**
     * Отменить запись.
     */
    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        return appointmentRepository.save(appointment);
    }

    /**
     * Найти запись по ID.
     */
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Запись не найдена"));
    }

    /**
     * Найти все записи (приемы) конкретного мастера.
     */
    public List<Appointment> findAppointmentsByBarber(Long barberId) {
        List<BarberService> barberServices = barberServiceRepository.findByBarberId(barberId);

        List<Long> barberServiceIds = barberServices.stream()
                .map(BarberService::getId)
                .toList();

        return appointmentRepository.findByBarberServiceIdIn(barberServiceIds);
    }

    /**
     * Получить все записи.
     */
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }
    public List<AppointmentResponseDto> getAllAppointmentsAsDto() {
        return appointmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AppointmentResponseDto convertToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();

        // Основные поля
        dto.setId(appointment.getId());
        dto.setAppointmentDateTime(appointment.getAppointmentDateTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setCreatedAt(appointment.getCreatedAt());

        // Информация о клиенте
        if (appointment.getClient() != null) {
            User client = appointment.getClient();
            dto.setClientId(client.getId());
            dto.setClientEmail(client.getEmail());

            // Объединяем имя и фамилию
            String clientFullName = "";
            if (client.getFirstName() != null) {
                clientFullName += client.getFirstName();
            }
            if (client.getLastName() != null) {
                if (!clientFullName.isEmpty()) {
                    clientFullName += " ";
                }
                clientFullName += client.getLastName();
            }
            // Если оба поля пустые - используем email
            dto.setClientName(clientFullName.isEmpty() ? client.getEmail() : clientFullName);
        }

        // Информация об услуге и мастере
        if (appointment.getBarberService() != null) {
            BarberService barberService = appointment.getBarberService();

            // Услуга
            if (barberService.getService() != null) {
                ServiceItem service = barberService.getService();
                dto.setServiceId(service.getId());
                dto.setServiceName(service.getName());
                dto.setServicePrice(service.getBasePrice()); // <-- ИСПРАВЛЕНО!
            }

            // Мастер
            if (barberService.getBarber() != null) {
                Barber barber = barberService.getBarber();
                dto.setBarberId(barber.getId());

                if (barber.getUser() != null) {
                    User barberUser = barber.getUser();
                    // Аналогично для имени мастера
                    String barberFullName = "";
                    if (barberUser.getFirstName() != null) {
                        barberFullName += barberUser.getFirstName();
                    }
                    if (barberUser.getLastName() != null) {
                        if (!barberFullName.isEmpty()) {
                            barberFullName += " ";
                        }
                        barberFullName += barberUser.getLastName();
                    }
                    dto.setBarberName(barberFullName.isEmpty() ? barberUser.getEmail() : barberFullName);
                }
            }
        }

        return dto;
    }
}