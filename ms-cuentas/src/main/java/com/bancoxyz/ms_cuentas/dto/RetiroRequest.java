package com.bancoxyz.ms_cuentas.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RetiroRequest(
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") Double monto) {
}
