package com.bancoxyz.bank_batch_system.common.client.dto;

public record RetiroResultadoClienteDTO(Long cuentaId, Double montoRetirado, Double saldoAnterior, Double saldoNuevo,
        String fecha) {
}
