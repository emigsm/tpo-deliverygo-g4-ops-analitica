package ar.edu.uade.api.deliverygo.excepcion;

/**
 * INT-01 a INT-04: un módulo externo no respondió, respondió vacío o devolvió datos inválidos. Se traduce a 503 (EXTERNAL_SERVICE_UNAVAILABLE).
 */
public class ExternalServiceUnavailableException extends RuntimeException {

    public ExternalServiceUnavailableException(String mensaje) {
        super(mensaje);
    }
}
