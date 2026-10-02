package com.bancoxyz.bank_batch_system.common.exception;

/**
 * Se lanza cuando un retiro solicitado supera el saldo disponible de la
 * cuenta. Ahora esta validación ocurre en ms-cuentas (el dueño de los
 * datos); el adaptador HTTP del BFF reconstruye esta excepción a partir del
 * mensaje 409 que devuelve el microservicio, para que el resto del BFF siga
 * manejando el mismo tipo de excepción que antes.
 */
public class FondosInsuficientesException extends RuntimeException {

    public FondosInsuficientesException(String message) {
        super(message);
    }
}