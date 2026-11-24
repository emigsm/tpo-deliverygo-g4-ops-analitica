package ar.edu.uade.api.deliverygo.excepcion;

/**
 * CU-01, CU-02: el id del pedido es nulo o no es positivo. Se traduce a 400 (INVALID_ORDER_ID).
 */
public class InvalidOrderIdException extends RuntimeException {

    public InvalidOrderIdException(String mensaje) {
        super(mensaje);
    }
}