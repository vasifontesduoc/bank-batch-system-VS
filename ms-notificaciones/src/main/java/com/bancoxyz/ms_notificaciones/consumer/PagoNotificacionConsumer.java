package com.bancoxyz.ms_notificaciones.consumer;

import com.bancoxyz.ms_notificaciones.event.PagoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PagoNotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(PagoNotificacionConsumer.class);

    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "${app.kafka.topic.pagos}",
                   groupId = "notificaciones-group-pagos",
                   containerFactory = "kafkaListenerContainerFactoryPagos")
    public void procesar(PagoEvent evento) {
        if (!eventosProcesados.add(evento.eventId())) {
            log.warn("Evento de pago duplicado detectado (eventId={}) para cuentaOrigenId={}, se omite",
                    evento.eventId(), evento.cuentaOrigenId());
            return;
        }

        log.info("ms-notificaciones recibio evento PagoRealizado -> cuentaOrigenId={}, monto={}, saldoResultante={}",
                evento.cuentaOrigenId(), evento.monto(), evento.saldoResultante());
    }
}
