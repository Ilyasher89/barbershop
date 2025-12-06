package com.example.barbershop.service;

import com.example.barbershop.entity.ServiceItem;
import com.example.barbershop.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с каталогом услуг.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public List<ServiceItem> getAllServices() {
        return serviceRepository.findAll();
    }

    public ServiceItem getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));
    }

    @Transactional
    public ServiceItem createService(String name, String description,
                                     Integer baseDurationMinutes, Double basePrice) {
        ServiceItem service = new ServiceItem();
        service.setName(name);
        service.setDescription(description);
        service.setBaseDurationMinutes(baseDurationMinutes);
        service.setBasePrice(basePrice);

        return serviceRepository.save(service);
    }

    @Transactional
    public ServiceItem updateService(Long id, ServiceItem updatedService) {
        ServiceItem service = getServiceById(id);

        if (updatedService.getName() != null) {
            service.setName(updatedService.getName());
        }
        if (updatedService.getDescription() != null) {
            service.setDescription(updatedService.getDescription());
        }
        if (updatedService.getBaseDurationMinutes() != null) {
            service.setBaseDurationMinutes(updatedService.getBaseDurationMinutes());
        }
        if (updatedService.getBasePrice() != null) {
            service.setBasePrice(updatedService.getBasePrice());
        }

        return serviceRepository.save(service);
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Услуга не найдена");
        }
        serviceRepository.deleteById(id);
    }
}