package com.bancoxyz.ms_cuentas.dto;

public record EstadoCuentaResponse(
        Long cuentaId,
        Integer cantidadMovimientos,
        Double totalDepositos,
        Double totalRetiros,
        Double totalOtros,
        Double saldoFinal,
        String fechaGeneracion) {
}
