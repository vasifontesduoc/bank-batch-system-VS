package com.bancoxyz.bank_batch_system.bff.mobile.service;

import com.bancoxyz.bank_batch_system.bff.mobile.dto.SaldoMobileDTO;
import com.bancoxyz.bank_batch_system.bff.mobile.dto.TransaccionMobileDTO;
import com.bancoxyz.bank_batch_system.bff.mobile.mapper.MobileMapper;
import com.bancoxyz.bank_batch_system.common.data.CuentaDataPort;
import com.bancoxyz.bank_batch_system.common.service.CuentaConsultaService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BFF Móvil: payloads livianos. Misma lógica de búsqueda de cuenta que los
 * otros canales (reutilizada desde CuentaConsultaService), pero transformada
 * al contrato mínimo del canal móvil mediante MobileMapper.
 */
@Service
public class MobileService {

    private final CuentaConsultaService consultaService;
    private final CuentaDataPort dataPort;
    private final MobileMapper mapper;

    public MobileService(CuentaConsultaService consultaService, CuentaDataPort dataPort, MobileMapper mapper) {
        this.consultaService = consultaService;
        this.dataPort = dataPort;
        this.mapper = mapper;
    }

    public SaldoMobileDTO obtenerSaldo(Long cuentaId) {
        return mapper.toSaldoDTO(consultaService.obtenerEstado(cuentaId));
    }

    public List<TransaccionMobileDTO> transaccionesRecientes(int limite) {
        return dataPort.listarTransaccionesRecientes(limite).stream().map(mapper::toTransaccionDTO).toList();
    }
}