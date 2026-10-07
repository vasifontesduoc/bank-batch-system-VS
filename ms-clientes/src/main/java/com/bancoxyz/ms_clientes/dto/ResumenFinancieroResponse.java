package com.bancoxyz.ms_clientes.dto;

public record ResumenFinancieroResponse(
        Long clienteId,
        Long cuentaId,
        Double saldoFinal,
        Integer cantidadMovimientos,
        boolean disponible,
        String mensaje) {
}
