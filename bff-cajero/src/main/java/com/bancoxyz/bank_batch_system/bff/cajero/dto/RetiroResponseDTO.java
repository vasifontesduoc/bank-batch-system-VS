package com.bancoxyz.bank_batch_system.bff.cajero.dto;

public record RetiroResponseDTO(
        Long cuentaId,
        Double montoRetirado,
        Double saldoAnterior,
        Double saldoNuevo,
        String fecha) {
}
