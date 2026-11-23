package ar.edu.uade.api.deliverygo.excepcion;

// Se utiliza cuando un módulo externo no responde (503)
/**
 * Un servicio externo no respondió, respondió vacío o devolvió datos inválidos.
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 503 con código EXTERNAL_SERVICE_UNAVAILABLE. Dentro de una transacción también provoca rollback.
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String mensaje) {
        super(mensaje);
    }
}
