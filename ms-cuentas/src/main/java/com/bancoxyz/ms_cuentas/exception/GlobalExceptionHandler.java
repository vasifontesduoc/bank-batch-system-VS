package com.bancoxyz.ms_cuentas.exception;

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
 * Mismo criterio de errores/códigos HTTP que el BFF (404 / 409 / 400 / 500),
 * para que la comunicación entre el BFF y este microservicio sea predecible:
 * el adaptador HTTP del BFF sabe exactamente qué código esperar para cada
 * tipo de problema.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, detalle.isBlank() ? "Datos de la solicitud inválidos" : detalle, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
