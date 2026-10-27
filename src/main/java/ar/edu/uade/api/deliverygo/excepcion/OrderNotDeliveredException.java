package ar.edu.uade.api.deliverygo.excepcion;

// RN-01: El pedido no existe o no ha sido entregado
public class OrderNotDeliveredException extends RuntimeException {
    public OrderNotDeliveredException(String mensaje) {
        super(mensaje);
    }
}
