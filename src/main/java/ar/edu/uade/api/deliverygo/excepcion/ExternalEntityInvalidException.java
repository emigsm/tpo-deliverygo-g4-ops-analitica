package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-08 (INT-02 a INT-04): el usuario, comercio o repartidor no existe o no está activo. Se traduce a 422 (BUSINESS_RULE_VIOLATION).
 */
public class ExternalEntityInvalidException extends RuntimeException {
    public ExternalEntityInvalidException(String mensaje) {
        super(mensaje);
    }
}
