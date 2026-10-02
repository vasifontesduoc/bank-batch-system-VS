package com.bancoxyz.bank_batch_system.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Cada microservicio (bff-web / bff-mobile / bff-cajero) ahora valida UNA
 * sola API Key, la suya propia, recibida en el header "X-API-KEY". Al ser
 * aplicaciones separadas, ya no hace falta distinguir entre 3 roles dentro
 * de un mismo proceso: si la key coincide, la petición queda autenticada.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-KEY";

    private final String apiKey;

    public ApiKeyAuthFilter(@Value("${bff.apikey}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String recibida = request.getHeader(HEADER);

        if (apiKey.equals(recibida)) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "cliente-autenticado", null, List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
