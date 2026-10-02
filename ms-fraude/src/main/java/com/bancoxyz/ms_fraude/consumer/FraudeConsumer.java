package com.bancoxyz.ms_fraude.consumer;

import com.bancoxyz.ms_fraude.event.RetiroEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FraudeConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudeConsumer.class);

    private final double umbral;

    public FraudeConsumer(@Value("${app.fraude.umbral}") double umbral) {
        this.umbral = umbral;
    }

    @KafkaListener(topics = "${app.kafka.topic.retiros}",
                   groupId = "fraude-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void procesar(RetiroEvent evento) {
        log.info("ms-fraude recibio evento RetiroRealizado -> cuentaId={}, monto={}",
                evento.cuentaId(), evento.monto());

        if (evento.monto() > umbral) {
            log.warn("ALERTA DE FRAUDE: retiro sospechoso en cuentaId={} por ${} (umbral=${}) - saldo resultante=${}",
                    evento.cuentaId(), evento.monto(), umbral, evento.saldoResultante());
        } else {
            log.info("Retiro dentro de parametros normales para cuentaId={}", evento.cuentaId());
        }
    }
}
