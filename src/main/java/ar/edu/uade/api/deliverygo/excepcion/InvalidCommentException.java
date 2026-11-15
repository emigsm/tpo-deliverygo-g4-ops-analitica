package ar.edu.uade.api.deliverygo.excepcion;

public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String mensaje) {
        super(mensaje);
    }
}
