package com.example.barbershop;

import com.example.barbershop.service.AppointmentService;
import com.example.barbershop.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootApplication
public class BarbershopApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbershopApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(UserService userService, AppointmentService appointmentService) {
		return args -> {
			// 1. Создаем тестовых пользователей
			userService.createTestUsers();

			// 2. Создаем тестовые услуги мастеров
			appointmentService.createTestBarberServices();

			System.out.println("=== ПРИЛОЖЕНИЕ ЗАПУЩЕНО И ГОТОВО К РАБОТЕ ===");
		};
	}
}