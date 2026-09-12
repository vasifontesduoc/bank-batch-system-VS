package com.bancoxyz.bank_batch_system.bff.cajero.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RetiroRequestDTO(
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") Double monto) {
}
