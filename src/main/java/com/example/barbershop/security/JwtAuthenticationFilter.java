package com.example.barbershop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Извлекаем JWT токен из заголовка Authorization ИЛИ из Cookie
            String jwt = parseJwt(request);

            // 2. Если токен есть и он валиден
            if (jwt != null && jwtUtils.extractEmail(jwt) != null) {
                String email = jwtUtils.extractEmail(jwt);

                // 3. Загружаем пользователя из БД
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 4. Проверяем валидность токена
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    // 5. Создаем объект аутентификации для Spring Security
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Устанавливаем аутентификацию в SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        // 7. Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлекает JWT токен из заголовка Authorization ИЛИ из Cookie.
     * Приоритет: заголовок Authorization, затем cookie "jwt-token".
     */
    private String parseJwt(HttpServletRequest request) {
        // 1. Проверяем заголовок Authorization (для API-клиентов типа Postman)
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Убираем "Bearer "
        }

        // 2. Проверяем cookie с именем "jwt-token" (для веб-браузера)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}