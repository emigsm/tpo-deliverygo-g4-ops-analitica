package ar.edu.uade.api.deliverygo.excepcion;

// Ya existe una reseña de este usuario para el pedido y tipo de objetivo (RN-02)
public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String mensaje) {
        super(mensaje);
    }
}
