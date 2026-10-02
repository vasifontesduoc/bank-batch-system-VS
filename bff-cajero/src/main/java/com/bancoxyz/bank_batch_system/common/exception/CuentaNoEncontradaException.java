package com.bancoxyz.bank_batch_system.common.exception;

/**
 * Se lanza cuando se consulta una cuenta que no tiene estado de cuenta anual
 * generado.
 */
public class CuentaNoEncontradaException extends RuntimeException {

    public CuentaNoEncontradaException(Long cuentaId) {
        super("No existe estado de cuenta para la cuenta " + cuentaId);
    }
}