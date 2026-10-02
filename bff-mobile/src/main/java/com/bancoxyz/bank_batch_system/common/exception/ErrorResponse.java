package com.bancoxyz.bank_batch_system.common.exception;

import java.time.LocalDateTime;

/**
 * Formato único de error para toda la API, sin importar el canal (Web,
 * Móvil, Cajero) ni el tipo de excepción. Mismos campos, mismo formato de
 * fecha, mismo criterio para el mensaje.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path);
    }
}