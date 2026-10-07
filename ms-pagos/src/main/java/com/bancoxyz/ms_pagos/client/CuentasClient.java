package com.bancoxyz.ms_pagos.client;

import com.bancoxyz.ms_pagos.dto.RetiroRequest;
import com.bancoxyz.ms_pagos.dto.RetiroResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CuentasClient {

    private static final Logger log = LoggerFactory.getLogger(CuentasClient.class);
    private static final String MS_CUENTAS_URL = "http://ms-cuentas/internal/cuentas/{cuentaId}/retiro";

    private final RestTemplate restTemplate;

    public CuentasClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "cuentasService", fallbackMethod = "retirarFallback")
    @Retry(name = "cuentasService")
    public RetiroResponse retirar(Long cuentaId, Double monto) {
        return restTemplate.postForObject(MS_CUENTAS_URL, new RetiroRequest(monto), RetiroResponse.class, cuentaId);
    }

    private RetiroResponse retirarFallback(Long cuentaId, Double monto, Throwable t) {
        log.error("FALLBACK: ms-cuentas no disponible para debitar cuentaId={}: {}", cuentaId, t.getMessage());
        return null;
    }
}
