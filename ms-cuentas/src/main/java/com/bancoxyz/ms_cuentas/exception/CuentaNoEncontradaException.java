package com.bancoxyz.ms_cuentas.exception;

public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long cuentaId) {
        super("No existe estado de cuenta para la cuenta " + cuentaId);
    }
}
