package com.example.barbershop.service;

import com.example.barbershop.entity.User;
import com.example.barbershop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Найти пользователя по ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Найти пользователя по email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Получить всех пользователей.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Регистрация нового пользователя.
     * Хеширует пароль и сохраняет в БД.
     */
    @Transactional
    public User registerUser(User user) {
        // Проверяем, что email свободен
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "Пользователь с email " + user.getEmail() + " уже существует"
            );
        }

        // Хэшируем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Устанавливаем роль по умолчанию, если не задана
        if (user.getRole() == null) {
            user.setRole(User.Role.CLIENT);
        }

        return userRepository.save(user);
    }

    /**
     * Создать тестовых пользователей (для разработки).
     */
    @Transactional
    public void createTestUsers() {
        // Только если нет пользователей в БД
        if (userRepository.count() == 0) {
            System.out.println("=== СОЗДАНИЕ ТЕСТОВЫХ ПОЛЬЗОВАТЕЛЕЙ ===");

            User admin = new User();
            admin.setEmail("admin@test.ru");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setFirstName("Админ");
            admin.setLastName("Админов");
            userRepository.save(admin);
            System.out.println("Создан ADMIN: " + admin.getEmail());

            User barber = new User();
            barber.setEmail("barber@test.ru");
            barber.setPassword(passwordEncoder.encode("barber123"));
            barber.setRole(User.Role.BARBER);
            barber.setFirstName("Иван");
            barber.setLastName("Парикмахеров");
            userRepository.save(barber);
            System.out.println("Создан BARBER: " + barber.getEmail());

            User client = new User();
            client.setEmail("client@test.ru");
            client.setPassword(passwordEncoder.encode("client123"));
            client.setRole(User.Role.CLIENT);
            client.setFirstName("Петр");
            client.setLastName("Клиентов");
            userRepository.save(client);
            System.out.println("Создан CLIENT: " + client.getEmail());

            System.out.println("=== ТЕСТОВЫЕ ПОЛЬЗОВАТЕЛИ СОЗДАНЫ ===");
        }
    }

    /**
     * Проверить пароль пользователя.
     */
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}