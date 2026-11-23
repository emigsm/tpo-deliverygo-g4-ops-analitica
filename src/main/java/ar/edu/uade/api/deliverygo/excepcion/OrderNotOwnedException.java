package ar.edu.uade.api.deliverygo.excepcion;

// El pedido no pertenece al usuario autenticado (RN-07)
/**
 * El pedido pertenece a otro usuario (RN-07).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 422. Alternativa: 403 Forbidden, que describe mejor el caso; se mantiene 422 como lo documenta OpenAPI en {@code OpinionesController}.
 */
public class OrderNotOwnedException extends RuntimeException {
    public OrderNotOwnedException(String mensaje) {
        super(mensaje);
    }
}
