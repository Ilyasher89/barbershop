package com.example.barbershop.security;

import com.example.barbershop.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Кастомная реализация UserDetails для хранения дополнительных полей пользователя.
 * Позволяет получать firstName, lastName, phoneNumber в Thymeleaf шаблонах.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Преобразуем роль в формат ROLE_* для Spring Security
        String roleWithPrefix = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // Стандартные методы - всегда true (если не нужна сложная логика)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ========== ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Получить имя пользователя (для приветствия)
     */
    public String getFirstName() {
        return user.getFirstName();
    }

    /**
     * Получить фамилию пользователя
     */
    public String getLastName() {
        return user.getLastName();
    }

    /**
     * Получить телефон пользователя
     */
    public String getPhoneNumber() {
        return user.getPhoneNumber();
    }

    /**
     * Получить полное имя (имя + фамилия)
     */
    public String getFullName() {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else {
            return user.getEmail();
        }
    }

    /**
     * Получить роль пользователя (без префикса ROLE_)
     */
    public String getRole() {
        return user.getRole().name();
    }

    /**
     * Получить ID пользователя
     */
    public Long getId() {
        return user.getId();
    }
}