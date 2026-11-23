package ar.edu.uade.api.deliverygo.excepcion;

//El usuario, comercio o repartidor no existe o no está activo (RN-08)
/**
 * El usuario, comercio o repartidor no existe o no está activo (RN-08, INT-02 a INT-04).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 422.
 */
public class ExternalEntityInvalidException extends RuntimeException {
    public ExternalEntityInvalidException(String mensaje) {
        super(mensaje);
    }
}
