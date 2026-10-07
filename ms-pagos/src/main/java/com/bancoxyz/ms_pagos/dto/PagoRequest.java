package com.bancoxyz.ms_pagos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PagoRequest(
        @NotNull(message = "La cuenta origen es obligatoria") Long cuentaOrigenId,
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a 0") Double monto,
        String descripcion) {
}
