package com.bancoxyz.ms_cuentas.dto;

public record TransaccionResponse(Long id, String fecha, Double monto, String tipo) {
}