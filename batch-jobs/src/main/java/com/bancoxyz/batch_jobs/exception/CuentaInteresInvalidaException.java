package com.bancoxyz.batch_jobs.exception;

public class CuentaInteresInvalidaException extends RuntimeException {
    public CuentaInteresInvalidaException(String mensaje) {
        super(mensaje);
    }
}
