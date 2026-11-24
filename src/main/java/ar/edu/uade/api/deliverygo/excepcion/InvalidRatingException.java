package ar.edu.uade.api.deliverygo.excepcion;

/**
 * RN-04 (CU-01, CU-02): la puntuación no está entre 1 y 5. Se traduce a 400 (INVALID_RATING).
 */
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String mensaje) {
        super(mensaje);
    }
}
