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
 * Autenticación por API Key: cada canal (Web, Móvil, Cajero) tiene su propia
 * key, enviada en el header "X-API-KEY". La key determina el rol
 * (ROLE_WEB / ROLE_MOBILE / ROLE_CAJERO) con el que se autoriza la petición;
 * SecurityConfig se encarga de exigir el rol correcto según el prefijo de la
 * URL (/api/web, /api/mobile, /api/cajero).
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-KEY";

    private final String webKey;
    private final String mobileKey;
    private final String cajeroKey;

    public ApiKeyAuthFilter(
            @Value("${bff.apikey.web}") String webKey,
            @Value("${bff.apikey.mobile}") String mobileKey,
            @Value("${bff.apikey.cajero}") String cajeroKey) {
        this.webKey = webKey;
        this.mobileKey = mobileKey;
        this.cajeroKey = cajeroKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String apiKey = request.getHeader(HEADER);
        String role = resolveRole(apiKey);

        if (role != null) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "canal-" + role.toLowerCase(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private String resolveRole(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        if (apiKey.equals(webKey)) {
            return "WEB";
        }
        if (apiKey.equals(mobileKey)) {
            return "MOBILE";
        }
        if (apiKey.equals(cajeroKey)) {
            return "CAJERO";
        }
        return null;
    }
}