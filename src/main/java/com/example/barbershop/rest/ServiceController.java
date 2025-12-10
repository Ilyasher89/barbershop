package com.example.barbershop.rest;

import com.example.barbershop.entity.ServiceItem;
import com.example.barbershop.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для работы с услугами парикмахерской.
 * Предоставляет API для получения информации об услугах.
 */
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    /**
     * Получить список всех услуг.
     * GET /api/services
     */
    @GetMapping
    public List<ServiceItem> getAllServices() {
        return serviceCatalogService.getAllServices();
    }

    /**
     * Получить услугу по ID.
     * GET /api/services/{id}
     */
    @GetMapping("/{id}")
    public ServiceItem getServiceById(@PathVariable Long id) {
        return serviceCatalogService.getServiceById(id);
    }

    /**
     * Создать новую услугу (только для администраторов).
     * POST /api/services
     * В теле запроса должен быть JSON с данными услуги.
     */
    @PostMapping
    public ServiceItem createService(@RequestBody ServiceItem service) {
        System.out.println("Создание услуги: " + service.getName());

        return service;
    }
}