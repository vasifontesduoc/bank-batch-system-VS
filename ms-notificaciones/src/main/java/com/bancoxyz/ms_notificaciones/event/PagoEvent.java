package com.bancoxyz.ms_notificaciones.event;

public record PagoEvent(
        String eventId,
        Long cuentaOrigenId,
        Double monto,
        Double saldoResultante,
        String descripcion,
        String fecha
) {
}
