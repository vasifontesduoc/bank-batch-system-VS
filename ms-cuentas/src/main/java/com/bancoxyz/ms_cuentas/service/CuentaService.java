package com.bancoxyz.ms_cuentas.service;

import com.bancoxyz.ms_cuentas.dto.*;
import com.bancoxyz.ms_cuentas.event.RetiroEvent;
import com.bancoxyz.ms_cuentas.exception.CuentaNoEncontradaException;
import com.bancoxyz.ms_cuentas.exception.FondosInsuficientesException;
import com.bancoxyz.ms_cuentas.messaging.RetiroEventProducer;
import com.bancoxyz.ms_cuentas.model.CuentaAnual;
import com.bancoxyz.ms_cuentas.model.EstadoCuentaAnual;
import com.bancoxyz.ms_cuentas.repository.CuentaAnualRepository;
import com.bancoxyz.ms_cuentas.repository.EstadoCuentaAnualRepository;
import com.bancoxyz.ms_cuentas.repository.InteresRepository;
import com.bancoxyz.ms_cuentas.repository.TransaccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Única capa de lógica de negocio/datos de todo el sistema. Como este
 * microservicio es el dueño de la fuente de verdad (la base de datos), la
 * regla de consistencia del retiro (validar fondos, actualizar saldo,
 * registrar el movimiento, todo en una transacción) vive aquí — no en el
 * BFF — para que cualquier canal o consumidor futuro de este microservicio
 * la reciba ya validada, sin poder saltársela.
 *
 * Tras confirmar el retiro, se publica el evento de dominio
 * "RetiroRealizado" en Kafka (retiros-topic). ms-cuentas no conoce ni
 * depende de quién consuma ese evento — así se conectan ms-fraude y
 * ms-notificaciones sin acoplarse a esta clase.
 */
@Service
public class CuentaService {

    private final EstadoCuentaAnualRepository estadoRepository;
    private final CuentaAnualRepository movimientosRepository;
    private final TransaccionRepository transaccionRepository;
    private final InteresRepository interesRepository;
    private final RetiroEventProducer retiroEventProducer;

    public CuentaService(EstadoCuentaAnualRepository estadoRepository,
            CuentaAnualRepository movimientosRepository,
            TransaccionRepository transaccionRepository,
            InteresRepository interesRepository,
            RetiroEventProducer retiroEventProducer) {
        this.estadoRepository = estadoRepository;
        this.movimientosRepository = movimientosRepository;
        this.transaccionRepository = transaccionRepository;
        this.interesRepository = interesRepository;
        this.retiroEventProducer = retiroEventProducer;
    }

    public EstadoCuentaResponse obtenerEstado(Long cuentaId) {
        EstadoCuentaAnual e = buscarEstado(cuentaId);
        return new EstadoCuentaResponse(e.getCuentaId(), e.getCantidadMovimientos(), e.getTotalDepositos(),
                e.getTotalRetiros(), e.getTotalOtros(), e.getSaldoFinal(), e.getFechaGeneracion());
    }

    public List<MovimientoResponse> obtenerMovimientos(Long cuentaId) {
        return movimientosRepository.findByCuentaIdOrderByFechaDesc(cuentaId).stream()
                .map(m -> new MovimientoResponse(m.getFecha(), m.getTransaccion(), m.getMonto(), m.getDescripcion()))
                .toList();
    }

    public List<TransaccionResponse> listarTransacciones() {
        return transaccionRepository.findAll().stream()
                .map(t -> new TransaccionResponse(t.getId(), t.getFecha(), t.getMonto(), t.getTipo()))
                .toList();
    }

    public List<TransaccionResponse> listarTransaccionesRecientes(int limite) {
        return transaccionRepository.findAll(PageRequest.of(0, limite, Sort.by("id").descending())).stream()
                .map(t -> new TransaccionResponse(t.getId(), t.getFecha(), t.getMonto(), t.getTipo()))
                .toList();
    }

    public List<InteresResponse> obtenerIntereses(Long cuentaId) {
        return interesRepository.findByCuentaId(cuentaId).stream()
                .map(i -> new InteresResponse(i.getId(), i.getCuentaId(), i.getNombre(), i.getEdad(), i.getTipo(),
                        i.getSaldo()))
                .toList();
    }

    @Transactional
    public RetiroResponse retirar(Long cuentaId, Double monto) {
        EstadoCuentaAnual estado = buscarEstado(cuentaId);
        double saldoActual = estado.getSaldoFinal();

        if (monto > saldoActual) {
            throw new FondosInsuficientesException(cuentaId, saldoActual, monto);
        }

        double saldoNuevo = saldoActual - monto;
        String hoy = LocalDate.now().toString();

        estado.setSaldoFinal(saldoNuevo);
        estado.setTotalRetiros(estado.getTotalRetiros() + monto);
        estado.setCantidadMovimientos(estado.getCantidadMovimientos() + 1);
        estadoRepository.save(estado);

        CuentaAnual movimiento = new CuentaAnual();
        movimiento.setCuentaId(cuentaId);
        movimiento.setFecha(hoy);
        movimiento.setTransaccion("RETIRO");
        movimiento.setMonto(-monto);
        movimiento.setDescripcion("Retiro cajero automático");
        movimientosRepository.save(movimiento);

        retiroEventProducer.publicarRetiroRealizado(
                new RetiroEvent(cuentaId, monto, saldoNuevo, "Retiro cajero automático",
                        LocalDateTime.now().toString()));

        return new RetiroResponse(cuentaId, monto, saldoActual, saldoNuevo, hoy);
    }

    private EstadoCuentaAnual buscarEstado(Long cuentaId) {
        return estadoRepository.findById(cuentaId)
                .orElseThrow(() -> new CuentaNoEncontradaException(cuentaId));
    }
}
