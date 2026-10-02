package com.bancoxyz.bank_batch_system.common.exception;

public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException() {
        super("El servicio de datos no está disponible en este momento. Intenta nuevamente en unos segundos.");
    }
}