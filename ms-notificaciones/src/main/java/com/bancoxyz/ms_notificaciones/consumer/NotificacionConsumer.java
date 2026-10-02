package com.bancoxyz.ms_notificaciones.consumer;

import com.bancoxyz.ms_notificaciones.event.RetiroEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificacionConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificacionConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic.retiros}",
                   groupId = "notificaciones-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void procesar(RetiroEvent evento) {
        log.info("ms-notificaciones recibio evento RetiroRealizado -> cuentaId={}, monto={}",
                evento.cuentaId(), evento.monto());

        log.info("NOTIFICACION ENVIADA al cliente de la cuenta {}: se realizo un retiro de ${}. " +
                        "Su saldo actual es ${}. Fecha: {}",
                evento.cuentaId(), evento.monto(), evento.saldoResultante(), evento.fecha());
    }
}
