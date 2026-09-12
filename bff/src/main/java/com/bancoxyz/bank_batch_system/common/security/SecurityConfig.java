package com.bancoxyz.bank_batch_system.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cada canal solo puede acceder a sus propios endpoints:
 * - /api/web/** exige ROLE_WEB
 * - /api/mobile/** exige ROLE_MOBILE
 * - /api/cajero/** exige ROLE_CAJERO
 *
 * Sin sesión (API stateless): la autenticación se resuelve en cada petición
 * a partir del header X-API-KEY (ver ApiKeyAuthFilter).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/web/**").hasRole("WEB")
                        .requestMatchers("/api/mobile/**").hasRole("MOBILE")
                        .requestMatchers("/api/cajero/**").hasRole("CAJERO")
                        .anyRequest().permitAll())
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
