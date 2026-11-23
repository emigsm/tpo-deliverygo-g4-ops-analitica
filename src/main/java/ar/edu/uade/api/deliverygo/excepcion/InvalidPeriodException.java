package ar.edu.uade.api.deliverygo.excepcion;

/**
 * Una fecha es nula o el período está invertido.
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_PERIOD.
 */
public class InvalidPeriodException extends RuntimeException {
    public InvalidPeriodException(String message) {
        super(message);
    }


}
