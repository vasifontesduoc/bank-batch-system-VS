package com.bancoxyz.bank_batch_system.bff.cajero.service;

import com.bancoxyz.bank_batch_system.bff.cajero.dto.RetiroResponseDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.dto.SaldoCajeroDTO;
import com.bancoxyz.bank_batch_system.bff.cajero.mapper.CajeroMapper;
import com.bancoxyz.bank_batch_system.common.service.CuentaConsultaService;
import com.bancoxyz.bank_batch_system.common.service.RetiroService;
import org.springframework.stereotype.Service;

/**
 * BFF Cajero: solo las dos operaciones críticas. La validación de fondos y
 * el registro del movimiento NO están aquí — viven en {@link RetiroService}
 * (common), reutilizable por cualquier otro canal que en el futuro necesite
 * ofrecer retiros.
 */
@Service
public class CajeroService {

    private final CuentaConsultaService consultaService;
    private final RetiroService retiroService;
    private final CajeroMapper mapper;

    public CajeroService(CuentaConsultaService consultaService, RetiroService retiroService, CajeroMapper mapper) {
        this.consultaService = consultaService;
        this.retiroService = retiroService;
        this.mapper = mapper;
    }

    public SaldoCajeroDTO consultarSaldo(Long cuentaId) {
        return mapper.toSaldoDTO(consultaService.obtenerEstado(cuentaId));
    }

    public RetiroResponseDTO retirar(Long cuentaId, Double monto) {
        return mapper.toRetiroResponseDTO(retiroService.ejecutar(cuentaId, monto));
    }
}
