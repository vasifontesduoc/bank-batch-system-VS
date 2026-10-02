package com.bancoxyz.bank_batch_system.bff.cajero.mapper;

import com.bancoxyz.bank_batch_system.bff.cajero.dto.RetiroResponseDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.dto.SaldoCajeroDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.RetiroResultadoClienteDTO;
import org.springframework.stereotype.Component;

@Component
public class CajeroMapper {

    public SaldoCajeroDTO toSaldoDTO(EstadoCuentaClienteDTO estado) {
        return new SaldoCajeroDTO(estado.cuentaId(), estado.saldoFinal());
    }

    public RetiroResponseDTO toRetiroResponseDTO(RetiroResultadoClienteDTO resultado) {
        return new RetiroResponseDTO(
                resultado.cuentaId(),
                resultado.montoRetirado(),
                resultado.saldoAnterior(),
                resultado.saldoNuevo(),
                resultado.fecha());
    }
}
