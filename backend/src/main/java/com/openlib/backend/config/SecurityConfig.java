package com.openlib.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilita CSRF para poder usar POST en el MVP
                .authorizeHttpRequests(auth -> auth
                        // Permite acceso total a las rutas de tu API
                        .requestMatchers("/api/users/**", "/api/orders/**", "/api/books/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> {}); // Permite autenticación básica si la necesitas después

        return http.build();
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}