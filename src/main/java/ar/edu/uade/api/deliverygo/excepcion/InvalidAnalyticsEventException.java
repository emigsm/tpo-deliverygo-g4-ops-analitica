package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-09 (CU-06): falta un campo obligatorio del evento, entidadId no es positivo o fechaEvento es futura. Se traduce a 400 (INVALID_ANALYTICS_EVENT).
 */
public class InvalidAnalyticsEventException extends RuntimeException {
    public InvalidAnalyticsEventException(String message) {

        super(message);
    }
}
