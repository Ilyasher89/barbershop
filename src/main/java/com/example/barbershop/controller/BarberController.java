package com.example.barbershop.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/barber")
public class BarberController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('BARBER')")
    public String barberDashboard(Model model) {
        model.addAttribute("message", "Панель парикмахера");
        return "barber/dashboard";
    }
}