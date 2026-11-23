package ar.edu.uade.api.deliverygo.excepcion;

//RN-04 -- La puntuación debe ser del 1 al 5
/**
 * La puntuación no está entre 1 y 5 (RN-04).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_RATING.
 */
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String mensaje) {
        super(mensaje);
    }
}
