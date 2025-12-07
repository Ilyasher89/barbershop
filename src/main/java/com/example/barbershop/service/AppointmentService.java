package com.example.barbershop.service;

import com.example.barbershop.dto.AppointmentResponseDto;
import com.example.barbershop.entity.*;
import com.example.barbershop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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

        // Проверяем доступность времени с учетом длительности услуги
        if (!isTimeSlotAvailable(barberService.getBarber().getId(), dateTime,
                barberService.getActualDurationMinutes())) {
            throw new IllegalArgumentException("Выбранное время занято. Выберите другое время.");
        }

        Appointment appointment = new Appointment();
        appointment.setClient(client);
        appointment.setBarberService(barberService);
        appointment.setAppointmentDateTime(dateTime);
        appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(LocalDateTime.now());

        log.info("Создана запись: клиент={}, мастер={}, время={}, услуга={}",
                client.getEmail(),
                barberService.getBarber().getUser().getEmail(),
                dateTime,
                barberService.getService().getName());

        return appointmentRepository.save(appointment);
    }

    /**
     * Проверить доступность временного слота для мастера с учетом длительности.
     * Проверяет ВСЕ услуги мастера на пересечение времени.
     */
    private boolean isTimeSlotAvailable(Long barberId, LocalDateTime newStartTime, Integer newDurationMinutes) {
        LocalDateTime newEndTime = newStartTime.plusMinutes(newDurationMinutes);

        log.info("🔍 Проверка времени: мастер={}, новое время={}-{} ({} мин)",
                barberId, newStartTime, newEndTime, newDurationMinutes);

        // 1. Найти ВСЕ существующие записи мастера (через все его услуги)
        List<Appointment> allBarberAppointments = new ArrayList<>();

        // Найти все услуги мастера
        List<BarberService> barberServices = barberServiceRepository.findByBarberId(barberId);

        // Для каждой услуги мастера найти все записи
        for (BarberService bs : barberServices) {
            List<Appointment> appointmentsForService = appointmentRepository.findByBarberServiceId(bs.getId());
            allBarberAppointments.addAll(appointmentsForService);
        }

        log.info("Найдено {} существующих записей мастера", allBarberAppointments.size());

        // 2. Проверить каждую существующую запись
        for (Appointment existing : allBarberAppointments) {
            // Пропускаем отмененные
            if (existing.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
                continue;
            }

            LocalDateTime existingStart = existing.getAppointmentDateTime();
            Integer existingDuration = existing.getBarberService().getActualDurationMinutes();
            LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

            log.info("  Существующая: {} - {} ({} мин, статус: {})",
                    existingStart, existingEnd, existingDuration, existing.getStatus());

            // Проверяем пересечение: новое начало ДО существующего конца
            // И новое конец ПОСЛЕ существующего начала
            boolean isOverlapping = newStartTime.isBefore(existingEnd) && newEndTime.isAfter(existingStart);

            if (isOverlapping) {
                log.warn("⛔ Время занято! Пересечение с записью ID {}: {} - {}",
                        existing.getId(), existingStart, existingEnd);
                return false;
            }
        }

        log.info("✅ Время свободно");
        return true;
    }

    /**
     * Создать тестовые услуги мастеров.
     */
    @Transactional
    public void createTestBarberServices() {
        if (barberServiceRepository.count() == 0) {
            log.info("=== СОЗДАНИЕ ТЕСТОВЫХ УСЛУГ МАСТЕРОВ ===");

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

                log.info("Создано 3 услуги");
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
                    bs.setActualPrice(service.getBasePrice());
                    bs.setActualDurationMinutes(service.getBaseDurationMinutes());
                    barberServiceRepository.save(bs);

                    log.info("Создана связь: {} - {} (ID: {})",
                            barber.getUser().getFirstName(), service.getName(), bs.getId());
                }
            }

            log.info("=== ГОТОВО: {} услуг мастеров ===", barberServiceRepository.count());
        }
    }

    /**
     * Создать тестовую запись для проверки.
     */
    @Transactional
    public void createTestAppointment() {
        if (appointmentRepository.count() == 0) {
            log.info("=== СОЗДАНИЕ ТЕСТОВОЙ ЗАПИСИ ===");

            // Найти клиента
            User client = userRepository.findByEmail("client@test.ru")
                    .orElseThrow(() -> new RuntimeException("Клиент client@test.ru не найден"));

            // Найти мастера
            User barberUser = userRepository.findByEmail("barber@test.ru")
                    .orElseThrow(() -> new RuntimeException("Мастер не найден"));

            Barber barber = barberRepository.findByUserId(barberUser.getId())
                    .orElseThrow(() -> new RuntimeException("Сущность Barber не найдена"));

            // Взять первую услугу мастера
            List<BarberService> barberServices = barberServiceRepository.findByBarberId(barber.getId());
            if (!barberServices.isEmpty()) {
                BarberService barberService = barberServices.get(0);

                // Создать запись на завтра в 10:00
                LocalDateTime tomorrow10am = LocalDateTime.now()
                        .plusDays(1)
                        .withHour(10)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                Appointment appointment = new Appointment();
                appointment.setClient(client);
                appointment.setBarberService(barberService);
                appointment.setAppointmentDateTime(tomorrow10am);
                appointment.setStatus(Appointment.AppointmentStatus.SCHEDULED);
                appointment.setCreatedAt(LocalDateTime.now());

                appointmentRepository.save(appointment);
                log.info("✅ Создана тестовая запись для мастера: {}", barberUser.getEmail());
            }
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
                .collect(Collectors.toList());

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
        log.debug("Конвертация записи ID {} в DTO", appointment.getId());

        AppointmentResponseDto dto = new AppointmentResponseDto();

        // Основные поля
        dto.setId(appointment.getId());
        dto.setAppointmentDateTime(appointment.getAppointmentDateTime());
        dto.setStatus(appointment.getStatus() != null ? appointment.getStatus().name() : "UNKNOWN");
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
                dto.setServicePrice(barberService.getActualPrice());
                dto.setServiceDurationMinutes(barberService.getActualDurationMinutes()); // Добавляем длительность
            }

            // Мастер
            if (barberService.getBarber() != null) {
                Barber barber = barberService.getBarber();
                dto.setBarberId(barber.getId());

                if (barber.getUser() != null) {
                    User barberUser = barber.getUser();
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

        log.debug("DTO создан: {}", dto);
        return dto;
    }
}