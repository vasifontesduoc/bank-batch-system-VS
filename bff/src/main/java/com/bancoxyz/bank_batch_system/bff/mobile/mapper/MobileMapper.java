package com.bancoxyz.bank_batch_system.bff.mobile.mapper;

import com.bancoxyz.bank_batch_system.bff.mobile.dto.SaldoMobileDTO;
import com.bancoxyz.bank_batch_system.bff.mobile.dto.TransaccionMobileDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.EstadoCuentaClienteDTO;
import com.bancoxyz.bank_batch_system.common.client.dto.TransaccionClienteDTO;
import org.springframework.stereotype.Component;

@Component
public class MobileMapper {

    public SaldoMobileDTO toSaldoDTO(EstadoCuentaClienteDTO estado) {
        return new SaldoMobileDTO(estado.cuentaId(), estado.saldoFinal());
    }

    public TransaccionMobileDTO toTransaccionDTO(TransaccionClienteDTO t) {
        return new TransaccionMobileDTO(t.fecha(), t.monto(), t.tipo());
    }
}
