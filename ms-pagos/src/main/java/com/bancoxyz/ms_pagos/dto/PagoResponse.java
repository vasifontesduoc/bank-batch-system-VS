package com.bancoxyz.ms_pagos.dto;

public record PagoResponse(
        Long id,
        Long cuentaOrigenId,
        Double monto,
        String descripcion,
        String estado,
        Double saldoAnterior,
        Double saldoNuevo,
        String fecha,
        String mensaje) {
}
