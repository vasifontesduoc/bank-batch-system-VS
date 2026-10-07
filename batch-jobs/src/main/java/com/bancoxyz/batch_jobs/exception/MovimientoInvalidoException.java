package com.bancoxyz.batch_jobs.exception;

public class MovimientoInvalidoException extends RuntimeException {
    public MovimientoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
