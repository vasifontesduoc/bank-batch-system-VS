package com.bancoxyz.ms_cuentas.exception;

public class FondosInsuficientesException extends RuntimeException {

    public FondosInsuficientesException(Long cuentaId, double saldoDisponible, double montoSolicitado) {
        super("Fondos insuficientes en la cuenta " + cuentaId + ": saldo disponible " + saldoDisponible
                + ", monto solicitado " + montoSolicitado);
    }
}