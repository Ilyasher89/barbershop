package com.example.barbershop.config;

import com.example.barbershop.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ========== НАСТРОЙКА ДОСТУПА ==========
                .authorizeHttpRequests(auth -> auth
                        // ----- ПУБЛИЧНЫЕ СТРАНИЦЫ (без авторизации) -----
                        .requestMatchers("/", "/home", "/login", "/register", "/about",
                                "/api-test", "/services").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()

                        // ----- API АУТЕНТИФИКАЦИИ (публичное) -----
                        .requestMatchers("/api/auth/**").permitAll()

                        // ----- ВЕБ-СТРАНИЦЫ С ПРОВЕРКОЙ РОЛЕЙ -----
                        .requestMatchers("/client/**").hasRole("CLIENT")      // Только CLIENT
                        .requestMatchers("/barber/**").hasRole("BARBER")      // Только BARBER
                        .requestMatchers("/admin/**").hasRole("ADMIN")        // Только ADMIN

                        // ----- API (требует аутентификации) -----
                        .requestMatchers("/api/**").authenticated()

                        // ----- ВСЕ ОСТАЛЬНОЕ -----
                        .anyRequest().authenticated()  // Все остальные страницы требуют входа
                )

                // ========== ФОРМА ЛОГИНА (для веб-интерфейса) ==========
                .formLogin(form -> form
                        .loginPage("/login")                    // Страница входа
                        .loginProcessingUrl("/login")           // URL для POST запроса входа
                        .defaultSuccessUrl("/", true)           // После успешного входа - на главную
                        .failureUrl("/login?error=true")        // При ошибке - обратно с ошибкой
                        .usernameParameter("username")          // Параметр для email (по умолчанию)
                        .passwordParameter("password")          // Параметр для пароля (по умолчанию)
                        .permitAll()                            // Доступ к форме входа для всех
                )

                // ========== ВЫХОД ИЗ СИСТЕМЫ ==========
                .logout(logout -> logout
                        .logoutUrl("/logout")                   // URL для выхода
                        .logoutSuccessUrl("/login?logout=true") // После выхода - на страницу входа
                        .invalidateHttpSession(true)            // Уничтожить сессию
                        .deleteCookies("JSESSIONID")            // Удалить cookie сессии
                        .permitAll()                            // Доступ для всех
                )

                // ========== УПРАВЛЕНИЕ СЕССИЯМИ ==========
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // Сессии для веб
                        .maximumSessions(1)                    // Максимум 1 сессия на пользователя
                        .maxSessionsPreventsLogin(false)       // Разрешить новую сессию, старую завершить
                )

                // ========== CSRF ЗАЩИТА ==========
                .csrf(csrf -> csrf
                                .ignoringRequestMatchers("/api/**", "/api/auth/**") // Отключаем CSRF для API
                        // Для веб-форм CSRF включен автоматически
                )

                // ========== JWT ФИЛЬТР (для API) ==========
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // ========== ОБРАБОТКА ОШИБОК ==========
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/access-denied")    // Страница "Доступ запрещен"
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}