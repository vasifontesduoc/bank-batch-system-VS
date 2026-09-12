package com.bancoxyz.bank_batch_system.bff.web.dto;

import java.util.List;

public record EstadoCuentaWebDTO(
        Long cuentaId,
        Integer cantidadMovimientos,
        Double totalDepositos,
        Double totalRetiros,
        Double totalOtros,
        Double saldoFinal,
        String fechaGeneracion,
        List<MovimientoWebDTO> movimientos) {
}