package com.example.barbershop.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CLIENT')")
    public String clientDashboard(Model model) {
        model.addAttribute("message", "Панель клиента");
        return "client/dashboard";
    }
}