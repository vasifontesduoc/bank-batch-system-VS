package com.bancoxyz.bank_batch_system.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Manejo de errores CENTRALIZADO y UNIFORME para los 3 BFF: mismo formato de
 * respuesta ({@link ErrorResponse}), mismo criterio de código HTTP por tipo
 * de problema, en un solo lugar en vez de repetido por controlador.
 *
 * Código HTTP usado por tipo de error:
 * - 404 Not Found -> el recurso solicitado (cuenta) no existe.
 * - 409 Conflict -> la operación es válida pero choca con el estado actual
 * (fondos insuficientes).
 * - 400 Bad Request -> el request está mal formado o no pasa las validaciones
 * (@Valid).
 * - 500 Internal Server Error -> cualquier error no anticipado.
 */
@RestControllerAdvice
public class BffExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BffExceptionHandler.class);

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleCuentaNoEncontrada(CuentaNoEncontradaException ex,
            HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(FondosInsuficientesException.class)
    public ResponseEntity<ErrorResponse> handleFondosInsuficientes(FondosInsuficientesException ex,
            HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    /** Errores de @Valid en el body (ej. RetiroRequestDTO con monto inválido). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, detalle.isBlank() ? "Datos de la solicitud inválidos" : detalle, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Cualquier otro error no anticipado: nunca se expone el detalle interno al
     * cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta nuevamente.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
