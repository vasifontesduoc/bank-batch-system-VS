package com.bancoxyz.batch_jobs.exception;

public class EstadoFinancieroInvalidoException extends RuntimeException {
    public EstadoFinancieroInvalidoException(String mensaje) {
        super(mensaje);
    }
}
