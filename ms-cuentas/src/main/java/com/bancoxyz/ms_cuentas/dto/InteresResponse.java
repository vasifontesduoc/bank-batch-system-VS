package com.bancoxyz.ms_cuentas.dto;

public record InteresResponse(Long id, Long cuentaId, String nombre, Integer edad, String tipo, Double saldo) {
}
