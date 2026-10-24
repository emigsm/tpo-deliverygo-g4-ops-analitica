package ar.edu.uade.api.deliverygo.excepcion;

//RN-04 -- La puntuación debe ser del 1 al 5
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String mensaje) {
        super(mensaje);
    }
}
