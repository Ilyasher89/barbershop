package com.example.barbershop.controller;

import com.example.barbershop.entity.Service;
import com.example.barbershop.service.ServiceCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Веб-контроллер для страниц услуг (Thymeleaf)
 */
@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class WebServiceController {

    private final ServiceCatalogService serviceCatalogService;

    /**
     * Страница со списком всех услуг
     * Доступна всем аутентифицированным пользователям
     */
    @GetMapping
    public String listServices(Model model) {
        List<Service> services = serviceCatalogService.getAllServices();
        model.addAttribute("services", services);
        model.addAttribute("totalServices", services.size());
        return "services/list";
    }

    /**
     * Страница создания новой услуги (ТЗ п.1.4 - добавление)
     * Только для администраторов
     */
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("isEdit", false);
        return "services/form";
    }

    /**
     * Обработка создания услуги
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String createService(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer baseDurationMinutes,
            @RequestParam Double basePrice) {

        serviceCatalogService.createService(name, description, baseDurationMinutes, basePrice);
        return "redirect:/services";
    }

    /**
     * Страница редактирования услуги (ТЗ п.1.4 - редактирование)
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        Service service = serviceCatalogService.getServiceById(id);
        model.addAttribute("service", service);
        model.addAttribute("isEdit", true);
        return "services/form";
    }

    /**
     * Обработка обновления услуги
     */
    @PostMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateService(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer baseDurationMinutes,
            @RequestParam Double basePrice) {

        Service updatedService = new Service();
        updatedService.setName(name);
        updatedService.setDescription(description);
        updatedService.setBaseDurationMinutes(baseDurationMinutes);
        updatedService.setBasePrice(basePrice);

        serviceCatalogService.updateService(id, updatedService);
        return "redirect:/services";
    }

    /**
     * Удаление услуги (ТЗ п.1.4 - удаление)
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteService(@PathVariable Long id) {
        serviceCatalogService.deleteService(id);
        return "redirect:/services";
    }

    /**
     * Страница детального просмотра услуги
     */
    @GetMapping("/{id}")
    public String viewService(@PathVariable Long id, Model model) {
        Service service = serviceCatalogService.getServiceById(id);
        model.addAttribute("service", service);
        return "services/view";
    }
}