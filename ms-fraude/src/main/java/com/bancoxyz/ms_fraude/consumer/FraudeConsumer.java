package com.bancoxyz.ms_fraude.consumer;

import com.bancoxyz.ms_fraude.event.RetiroEvent;
import com.bancoxyz.ms_fraude.service.AntifraudeExternoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FraudeConsumer {

    private static final Logger log = LoggerFactory.getLogger(FraudeConsumer.class);

    private final double umbral;
    private final AntifraudeExternoService antifraudeExternoService;

    private final Set<String> eventosProcesados = ConcurrentHashMap.newKeySet();

    public FraudeConsumer(@Value("${app.fraude.umbral}") double umbral,
                           AntifraudeExternoService antifraudeExternoService) {
        this.umbral = umbral;
        this.antifraudeExternoService = antifraudeExternoService;
    }

    @KafkaListener(topics = "${app.kafka.topic.retiros}",
                   groupId = "fraude-group",
                   containerFactory = "kafkaListenerContainerFactory")
    public void procesar(RetiroEvent evento) {
        if (!eventosProcesados.add(evento.eventId())) {
            log.warn("Evento duplicado detectado (eventId={}) para cuentaId={}, se omite reprocesamiento",
                    evento.eventId(), evento.cuentaId());
            return;
        }

        log.info("ms-fraude recibio evento RetiroRealizado -> cuentaId={}, monto={}",
                evento.cuentaId(), evento.monto());

        antifraudeExternoService.validarConProveedorExterno(evento);

        if (evento.monto() > umbral) {
            log.warn("ALERTA DE FRAUDE: retiro sospechoso en cuentaId={} por ${} (umbral=${}) - saldo resultante=${}",
                    evento.cuentaId(), evento.monto(), umbral, evento.saldoResultante());
        } else {
            log.info("Retiro dentro de parametros normales para cuentaId={}", evento.cuentaId());
        }
    }
}
