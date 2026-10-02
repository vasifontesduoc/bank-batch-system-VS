package com.bancoxyz.ms_fraude.event;

public record RetiroEvent(
        Long cuentaId,
        Double monto,
        Double saldoResultante,
        String descripcion,
        String fecha
) {
}
