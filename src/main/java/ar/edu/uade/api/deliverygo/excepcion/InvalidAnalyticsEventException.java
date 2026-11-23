package ar.edu.uade.api.deliverygo.excepcion;

/**
 * Falta algún campo obligatorio de un evento analítico (RN-09).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_ANALYTICS_EVENT.
 */
public class InvalidAnalyticsEventException extends RuntimeException {
    public InvalidAnalyticsEventException(String message) {

        super(message);
    }
}
