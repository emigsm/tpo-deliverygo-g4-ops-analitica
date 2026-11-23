package ar.edu.uade.api.deliverygo.excepcion;

/**
 * El identificador del pedido es nulo o no positivo.
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 400 con código INVALID_ORDER_ID.
 */
public class InvalidOrderIdException extends RuntimeException {

    public InvalidOrderIdException(String mensaje) {
        super(mensaje);
    }
}