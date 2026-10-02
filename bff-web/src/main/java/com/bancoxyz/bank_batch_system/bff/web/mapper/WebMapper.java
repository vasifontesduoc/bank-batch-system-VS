package com.bancoxyz.bank_batch_system.bff.web.mapper;

import com.bancoxyz.bank_batch_system.bff.web.dto.EstadoCuentaWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.InteresWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.MovimientoWebDTO;
import com.bancoxyz.bank_batch_system.bff.web.dto.TransaccionWebDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.InteresClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.MovimientoClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.TransaccionClienteDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Única clase responsable de transformar lo que devuelve ms-cuentas en los
 * DTO del canal Web. Ningún service arma DTOs "a mano" con .stream().map()
 * disperso; todo pasa por aquí.
 */
@Component
public class WebMapper {

    private static final double UMBRAL_ANOMALIA = 3000.0;

    public EstadoCuentaWebDTO toEstadoCuentaDTO(EstadoCuentaClienteDTO estado, List<MovimientoClienteDTO> movimientos) {
        return new EstadoCuentaWebDTO(
                estado.cuentaId(),
                estado.cantidadMovimientos(),
                estado.totalDepositos(),
                estado.totalRetiros(),
                estado.totalOtros(),
                estado.saldoFinal(),
                estado.fechaGeneracion(),
                movimientos.stream().map(this::toMovimientoDTO).toList());
    }

    public MovimientoWebDTO toMovimientoDTO(MovimientoClienteDTO m) {
        return new MovimientoWebDTO(m.fecha(), m.transaccion(), m.monto(), m.descripcion());
    }

    public TransaccionWebDTO toTransaccionDTO(TransaccionClienteDTO t) {
        return new TransaccionWebDTO(t.id(), t.fecha(), t.monto(), t.tipo(), esAnomalia(t.monto()));
    }

    public InteresWebDTO toInteresDTO(InteresClienteDTO i) {
        return new InteresWebDTO(i.id(), i.cuentaId(), i.nombre(), i.edad(), i.tipo(), i.saldo());
    }

    private boolean esAnomalia(Double monto) {
        return monto != null && (monto <= 0.0 || monto >= UMBRAL_ANOMALIA);
    }
}