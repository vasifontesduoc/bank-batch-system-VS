package com.bancoxyz.ms_cuentas.messaging;

import com.bancoxyz.ms_cuentas.event.RetiroEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

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

    public void publicarRetiroRealizado(RetiroEvent evento) {
        kafkaTemplate.send(topic, evento.cuentaId().toString(), evento)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento RetiroRealizado publicado en '{}' -> cuentaId={}, monto={}",
                                topic, evento.cuentaId(), evento.monto());
                    } else {
                        log.error("Error publicando evento RetiroRealizado para cuentaId={}: {}",
                                evento.cuentaId(), ex.getMessage());
                    }
                });
    }
}
