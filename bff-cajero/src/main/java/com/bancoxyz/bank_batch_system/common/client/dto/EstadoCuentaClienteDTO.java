package com.bancoxyz.bank_batch_system.common.client.dto;

public record EstadoCuentaClienteDTO(
        Long cuentaId,
        Integer cantidadMovimientos,
        Double totalDepositos,
        Double totalRetiros,
        Double totalOtros,
        Double saldoFinal,
        String fechaGeneracion) {
}