package com.bancoxyz.ms_notificaciones.service;

import com.bancoxyz.ms_notificaciones.event.RetiroEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Simula el envio a un proveedor externo de SMS/email. Protegido con
 * Resilience4j: si el proveedor falla, se degrada con un fallback en vez
 * de perder la notificacion silenciosamente.
 */
@Service
public class ProveedorNotificacionService {

    private static final Logger log = LoggerFactory.getLogger(ProveedorNotificacionService.class);

    @Value("${app.simular.falla.notificacion:false}")
    private boolean simularFalla;

    @CircuitBreaker(name = "enviarNotificacion", fallbackMethod = "fallbackEnvio")
    @Retry(name = "enviarNotificacion", fallbackMethod = "fallbackEnvio")
    public void enviarAlProveedorExterno(RetiroEvent evento) {
        if (simularFalla) {
            throw new RuntimeException("Proveedor de notificaciones (SMS/email) no disponible (simulado)");
        }
        log.info("NOTIFICACION ENVIADA al cliente de la cuenta {}: se realizo un retiro de ${}. " +
                        "Su saldo actual es ${}. Fecha: {}",
                evento.cuentaId(), evento.monto(), evento.saldoResultante(), evento.fecha());
    }

    // Se invoca por Retry agotado o por el Circuit Breaker abierto
    public void fallbackEnvio(RetiroEvent evento, Throwable t) {
        log.warn("FALLBACK: no se pudo notificar a la cuenta {} via el proveedor externo ({}). " +
                        "Se deja pendiente para reintento posterior.",
                evento.cuentaId(), t.getMessage());
    }
}
