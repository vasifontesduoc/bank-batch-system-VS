package com.bancoxyz.ms_notificaciones.consumer;

import com.bancoxyz.ms_notificaciones.event.RetiroEvent;
import com.bancoxyz.ms_notificaciones.service.ProveedorNotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    private final ProveedorNotificacionService proveedorNotificacionService;

    // Idempotencia: evita notificar dos veces al cliente si el mismo evento
    // llega reentregado (ej. por un rebalance de consumer group).
    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    public NotificacionConsumer(ProveedorNotificacionService proveedorNotificacionService) {
        this.proveedorNotificacionService = proveedorNotificacionService;
    }

    @KafkaListener(topics = "${app.kafka.topic.retiros}",
                   groupId = "notificaciones-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void procesar(RetiroEvent evento) {
        if (!eventosProcesados.add(evento.eventId())) {
            log.warn("Evento duplicado detectado (eventId={}) para cuentaId={}, se omite reenvio de notificacion",
                    evento.eventId(), evento.cuentaId());
            return;
        }

        log.info("ms-notificaciones recibio evento RetiroRealizado -> cuentaId={}, monto={}",
                evento.cuentaId(), evento.monto());

        proveedorNotificacionService.enviarAlProveedorExterno(evento);
    }
}
