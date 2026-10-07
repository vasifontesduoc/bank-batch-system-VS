package com.bancoxyz.ms_pagos.service;

import com.bancoxyz.ms_pagos.client.CuentasClient;
import com.bancoxyz.ms_pagos.dto.*;
import com.bancoxyz.ms_pagos.event.PagoEvent;
import com.bancoxyz.ms_pagos.messaging.PagoEventProducer;
import com.bancoxyz.ms_pagos.model.Pago;
import com.bancoxyz.ms_pagos.repository.PagoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PagoService {

    private final PagoRepository repository;
    private final CuentasClient cuentasClient;
    private final PagoEventProducer eventProducer;

    public PagoService(PagoRepository repository, CuentasClient cuentasClient, PagoEventProducer eventProducer) {
        this.repository = repository;
        this.cuentasClient = cuentasClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public PagoResponse procesarPago(PagoRequest request) {
        RetiroResponse retiro = cuentasClient.retirar(request.cuentaOrigenId(), request.monto());

        Pago pago = new Pago();
        pago.setCuentaOrigenId(request.cuentaOrigenId());
        pago.setMonto(request.monto());
        pago.setDescripcion(request.descripcion());
        pago.setFecha(LocalDateTime.now());

        if (retiro == null) {
            pago.setEstado("FALLIDO");
            Pago guardado = repository.save(pago);
            return toResponse(guardado, "No fue posible procesar el pago: ms-cuentas no disponible en este momento");
        }

        pago.setEstado("COMPLETADO");
        pago.setSaldoAnterior(retiro.saldoAnterior());
        pago.setSaldoNuevo(retiro.saldoNuevo());
        Pago guardado = repository.save(pago);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventProducer.publicarPagoRealizado(new PagoEvent(
                        UUID.randomUUID().toString(),
                        guardado.getCuentaOrigenId(),
                        guardado.getMonto(),
                        guardado.getSaldoNuevo(),
                        guardado.getDescripcion(),
                        guardado.getFecha().toString()
                ));
            }
        });

        return toResponse(guardado, "OK");
    }

    public PagoResponse obtener(Long id) {
        Pago pago = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + id));
        return toResponse(pago, pago.getEstado());
    }

    public List<PagoResponse> listar() {
        return repository.findAll().stream().map(p -> toResponse(p, p.getEstado())).toList();
    }

    private PagoResponse toResponse(Pago p, String mensaje) {
        return new PagoResponse(
                p.getId(), p.getCuentaOrigenId(), p.getMonto(), p.getDescripcion(), p.getEstado(),
                p.getSaldoAnterior(), p.getSaldoNuevo(),
                p.getFecha() == null ? null : p.getFecha().toString(), mensaje);
    }
}
