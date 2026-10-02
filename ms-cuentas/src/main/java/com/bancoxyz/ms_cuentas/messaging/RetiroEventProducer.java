package com.bancoxyz.ms_cuentas.messaging;

import com.bancoxyz.ms_cuentas.event.RetiroEvent;
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
public class RetiroEventProducer {

    private static final Logger log = LoggerFactory.getLogger(RetiroEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public RetiroEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${app.kafka.topic.retiros}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Retry(name = "publicarEvento", fallbackMethod = "fallbackPublicar")
    public void publicarRetiroRealizado(RetiroEvent evento) {
        try {
            kafkaTemplate.send(topic, evento.cuentaId().toString(), evento).get(3, TimeUnit.SECONDS);
            log.info("Evento RetiroRealizado publicado en '{}' -> cuentaId={}, monto={}",
                    topic, evento.cuentaId(), evento.monto());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Intento fallido al publicar evento RetiroRealizado para cuentaId={}: {}",
                    evento.cuentaId(), e.getMessage());
            throw new RuntimeException("Fallo al publicar evento a Kafka", e);
        }
    }

    // Se invoca cuando Retry agota los 3 intentos configurados (resilience4j.retry.instances.publicarEvento)
    public void fallbackPublicar(RetiroEvent evento, Throwable t) {
        log.error("FALLBACK: no fue posible publicar el evento RetiroRealizado para cuentaId={} " +
                        "tras agotar los reintentos ({}). El retiro ya quedo persistido en BD; " +
                        "el evento debe reprocesarse manualmente o via un mecanismo de outbox.",
                evento.cuentaId(), t.getMessage());
    }
}
