package com.bancoxyz.ms_notificaciones.event;

public record RetiroEvent(
        Long cuentaId,
        Double monto,
        Double saldoResultante,
        String descripcion,
        String fecha
) {
}
