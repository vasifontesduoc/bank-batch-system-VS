package com.bancoxyz.ms_fraude.event;

public record RetiroEvent(
        String eventId,
        Long cuentaId,
        Double monto,
        Double saldoResultante,
        String descripcion,
        String fecha
) {
}
