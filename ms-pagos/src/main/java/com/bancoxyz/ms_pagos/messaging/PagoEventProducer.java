package com.bancoxyz.ms_pagos.messaging;

import com.bancoxyz.ms_pagos.event.PagoEvent;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class PagoEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PagoEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public PagoEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                              @Value("${app.kafka.topic.pagos}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Retry(name = "publicarEventoPago", fallbackMethod = "fallbackPublicar")
    public void publicarPagoRealizado(PagoEvent evento) {
        try {
            kafkaTemplate.send(topic, evento.cuentaOrigenId().toString(), evento).get(3, TimeUnit.SECONDS);
            log.info("Evento PagoRealizado publicado en '{}' -> cuentaOrigenId={}, monto={}",
                    topic, evento.cuentaOrigenId(), evento.monto());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Intento fallido al publicar evento PagoRealizado para cuentaOrigenId={}: {}",
                    evento.cuentaOrigenId(), e.getMessage());
            throw new RuntimeException("Fallo al publicar evento a Kafka", e);
        }
    }

    public void fallbackPublicar(PagoEvent evento, Throwable t) {
        log.error("FALLBACK: no fue posible publicar el evento PagoRealizado para cuentaOrigenId={} " +
                        "tras agotar los reintentos ({}). El pago ya quedo persistido en BD.",
                evento.cuentaOrigenId(), t.getMessage());
    }
}
