package com.bancoxyz.ms_clientes.client;

import com.bancoxyz.ms_clientes.dto.EstadoCuentaResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CuentasClient {

    private static final String MS_CUENTAS_URL = "http://ms-cuentas/internal/cuentas/{cuentaId}/estado";

    private final RestTemplate restTemplate;

    public CuentasClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "estadoFallback")
    @Retry(name = "cuentasService")
    public EstadoCuentaResponse obtenerEstado(Long cuentaId) {
        return restTemplate.getForObject(MS_CUENTAS_URL, EstadoCuentaResponse.class, cuentaId);
    }

    private EstadoCuentaResponse estadoFallback(Long cuentaId, Throwable t) {
        return new EstadoCuentaResponse(cuentaId, 0, 0.0, 0.0, 0.0, null, null);
    }
}
