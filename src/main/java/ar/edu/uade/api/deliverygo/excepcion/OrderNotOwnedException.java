package ar.edu.uade.api.deliverygo.excepcion;

// El pedido no pertenece al usuario autenticado (RN-07)
public class OrderNotOwnedException extends RuntimeException {
    public OrderNotOwnedException(String mensaje) {
        super(mensaje);
    }
}
