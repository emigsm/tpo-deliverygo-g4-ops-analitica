package ar.edu.uade.api.deliverygo.excepcion;

/**
 * El tipo de objetivo es obligatorio y falta.
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_TARGET_TYPE.
 */
public class InvalidTargetTypeException extends RuntimeException {

    public InvalidTargetTypeException(String mensaje) {
        super(mensaje);
    }
}
