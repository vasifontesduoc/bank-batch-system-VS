package com.bancoxyz.ms_fraude.service;

import com.bancoxyz.ms_fraude.event.RetiroEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Simula la consulta a un proveedor antifraude externo (ej. un scoring de riesgo
 * de un tercero). Protegido con Resilience4j para que, si el proveedor externo
 * falla, el sistema se degrade con gracia en vez de perder el evento.
 */
@Service
public class AntifraudeExternoService {

    private static final Logger log = LoggerFactory.getLogger(AntifraudeExternoService.class);

    @Value("${app.simular.falla.antifraude:false}")
    private boolean simularFalla;

    @CircuitBreaker(name = "procesarEvento", fallbackMethod = "fallbackValidacion")
    @Retry(name = "procesarEvento", fallbackMethod = "fallbackValidacion")
    public void validarConProveedorExterno(RetiroEvent evento) {
        if (simularFalla) {
            throw new RuntimeException("Proveedor antifraude externo no disponible (simulado)");
        }
        log.info("Validacion con proveedor antifraude externo OK para cuentaId={}", evento.cuentaId());
    }

    // Se invoca por Retry agotado o por el Circuit Breaker abierto
    public void fallbackValidacion(RetiroEvent evento, Throwable t) {
        log.warn("FALLBACK: no se pudo validar con el proveedor antifraude externo para cuentaId={} ({}). " +
                        "Se continua solo con la regla local de umbral.",
                evento.cuentaId(), t.getMessage());
    }
}
