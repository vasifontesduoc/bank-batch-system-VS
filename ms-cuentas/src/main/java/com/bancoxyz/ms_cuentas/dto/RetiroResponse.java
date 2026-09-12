package com.bancoxyz.ms_cuentas.dto;

public record RetiroResponse(
        Long cuentaId,
        Double montoRetirado,
        Double saldoAnterior,
        Double saldoNuevo,
        String fecha) {
}
