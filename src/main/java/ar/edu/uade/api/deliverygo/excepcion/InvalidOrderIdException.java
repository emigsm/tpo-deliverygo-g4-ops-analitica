package ar.edu.uade.api.deliverygo.excepcion;

public class InvalidOrderIdException extends RuntimeException {

    public InvalidOrderIdException(String mensaje) {
        super(mensaje);
    }
}