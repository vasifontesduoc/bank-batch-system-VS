package com.bancoxyz.ms_cuentas.dto;

public record MovimientoResponse(String fecha, String transaccion, Double monto, String descripcion) {
}
