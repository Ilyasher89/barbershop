package com.example.barbershop.rest;

import com.example.barbershop.dto.BarberServiceDto;
import com.example.barbershop.entity.BarberService;
import com.example.barbershop.repository.BarberServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/barber-services")
@RequiredArgsConstructor
public class BarberServiceController {

    private final BarberServiceRepository barberServiceRepository;

    @GetMapping
    public List<BarberServiceDto> getAllBarberServices() {
        return barberServiceRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private BarberServiceDto convertToDto(BarberService barberService) {
        BarberServiceDto dto = new BarberServiceDto();
        dto.setId(barberService.getId());

        if (barberService.getBarber() != null) {
            dto.setBarberId(barberService.getBarber().getId());
            dto.setBarberName(barberService.getBarber().getUser().getFirstName() + " " +
                    barberService.getBarber().getUser().getLastName());
            dto.setBarberSpecialization(barberService.getBarber().getSpecialization());
        }

        if (barberService.getService() != null) {
            dto.setServiceId(barberService.getService().getId());
            dto.setServiceName(barberService.getService().getName());
            dto.setServiceDescription(barberService.getService().getDescription());
        }

        dto.setActualPrice(BigDecimal.valueOf(barberService.getActualPrice()));
        dto.setActualDurationMinutes(barberService.getActualDurationMinutes());

        return dto;
    }
}