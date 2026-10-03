package com.bancoxyz.bank_batch_system.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collection;
import java.util.List;

/**
 * Todas las rutas /api/** de este microservicio aceptan DOS formas de
 * autenticacion, cualquiera de las dos deja al request con ROLE_API_CLIENT:
 *   1) API Key propia (header X-API-KEY)         -> ApiKeyAuthFilter
 *   2) JWT emitido por auth-server (Bearer token) -> oauth2ResourceServer
 * Sin sesion (API stateless).
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
                        .requestMatchers("/api/**").hasRole("API_CLIENT")
                        .anyRequest().permitAll())
                .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Traduce el scope "api.access" del JWT a ROLE_API_CLIENT, para que
     * authorizeHttpRequests() no tenga que distinguir entre API Key y JWT.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("SCOPE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> scopeAuthorities = scopesConverter.convert(jwt);
            boolean tieneAccesoApi = scopeAuthorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("SCOPE_api.access"));
            if (tieneAccesoApi) {
                return List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"));
            }
            return List.of();
        });
        return converter;
    }
}
