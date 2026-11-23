package ar.edu.uade.api.deliverygo.excepcion;

// Ya existe una reseña de este usuario para el pedido y tipo de objetivo (RN-02)
/**
 * Ya existe una reseña del mismo usuario, pedido y tipo de objetivo (RN-02).
 *
 * Decisión: como es {@code RuntimeException}, un lanzamiento dentro de {@code @Transactional} revierte la transacción.
 * Se traduce a 409 con código DUPLICATE_REVIEW.
 */
public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String mensaje) {
        super(mensaje);
    }
}
