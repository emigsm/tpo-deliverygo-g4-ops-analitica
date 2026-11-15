package ar.edu.uade.api.deliverygo.excepcion;

public class InvalidTargetTypeException extends RuntimeException {

    public InvalidTargetTypeException(String mensaje) {
        super(mensaje);
    }
}
