package com.bancoxyz.bank_batch_system.common.client.dto;

public record InteresClienteDTO(Long id, Long cuentaId, String nombre, Integer edad, String tipo, Double saldo) {
}
