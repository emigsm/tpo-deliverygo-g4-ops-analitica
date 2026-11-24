package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-02 (CU-01, CU-02): ya existe una reseña del mismo usuario, pedido y tipo de objetivo. Se traduce a 409 (DUPLICATE_REVIEW).
 */
public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String mensaje) {
        super(mensaje);
    }
}
