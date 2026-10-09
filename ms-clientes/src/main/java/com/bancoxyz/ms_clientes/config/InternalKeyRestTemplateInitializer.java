package com.bancoxyz.ms_clientes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Agrega la clave interna (X-Internal-Key) a toda llamada saliente hacia
 * los microservicios protegidos.
 */
@Component
public class InternalKeyRestTemplateInitializer {

    public InternalKeyRestTemplateInitializer(RestTemplate restTemplate,
                                              @Value("${internal.api.key}") String key) {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("X-Internal-Key", key);
            return execution.execute(request, body);
        });
    }
}
