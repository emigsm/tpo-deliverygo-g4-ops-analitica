package ar.edu.uade.api.deliverygo.excepcion;

// RN-01: El pedido no existe o no ha sido entregado
/**
 * El pedido no existe o no está en estado ENTREGADO (RN-01).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 422. Decisión: un pedido inexistente se trata como no entregado; ver guía de observaciones.
 */
public class OrderNotDeliveredException extends RuntimeException {
    public OrderNotDeliveredException(String mensaje) {
        super(mensaje);
    }
}
